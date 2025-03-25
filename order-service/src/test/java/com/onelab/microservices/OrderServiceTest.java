package com.onelab.microservices;

import com.onelab.microservices.dto.*;
import com.onelab.dto.*;
import com.onelab.microservices.event.KafkaProducerService;
import com.onelab.microservices.feign.OrderFeignInterface;
import com.onelab.microservices.feign.UserFeignInterface;
import com.onelab.microservices.model.Order;
import com.onelab.microservices.model.OrderItem;
import com.onelab.microservices.repository.OrderRepository;
import com.onelab.microservices.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderFeignInterface orderFeignInterface;

    @Mock
    private UserFeignInterface userFeignInterface;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private OrderService orderService;

    private final String AUTH_HEADER = "Bearer token";

    private OrderRequestDTO orderRequestDTO;
    private OrderResponseDTO expectedResponse;
    private Order order;

    @BeforeEach
    void setUp() {
        OrderItemDTO orderItemDTO = new OrderItemDTO(1L, "ProductA", 2);
        orderRequestDTO = new OrderRequestDTO("CustomerTest", List.of(orderItemDTO));

        order = new Order(1L, "CustomerTest", new ArrayList<>());
        OrderItem orderItem = new OrderItem(null, 1L, "ProductA", 2, order);
        order.getItems().add(orderItem);

        expectedResponse = new OrderResponseDTO(1L, "CustomerTest", List.of(orderItemDTO));
    }

    @Test
    void createOrder_whenValidRequest_thenShouldSaveAndReturn() {
        when(userFeignInterface.validateUser(AUTH_HEADER)).thenReturn(ResponseEntity.ok().build());
        InventoryRequestDTO inventoryRequest = new InventoryRequestDTO("ProductA", 2, "CustomerTest");
        InventoryResponseDTO inventoryResponse = new InventoryResponseDTO();
        inventoryResponse.setAvailable(true);

        when(orderFeignInterface.reserveProduct(inventoryRequest)).thenReturn(ResponseEntity.ok(inventoryResponse));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });

        OrderResponseDTO response = orderService.createOrder(orderRequestDTO, AUTH_HEADER);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCustomerName()).isEqualTo("CustomerTest");
        assertThat(response.getItems().size()).isEqualTo(1);
        assertThat(response.getItems().get(0).getProductName()).isEqualTo("ProductA");
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(2);

        verify(kafkaProducerService, times(1)).sendMessage(eq("order-events"), eq("CREATE"), any(InventoryRequestDTO.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void getOrder_whenValid_thenReturnOrderResponse() {
        when(userFeignInterface.validateUser(AUTH_HEADER)).thenReturn(ResponseEntity.ok().build());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponseDTO response = orderService.getOrder(1L, AUTH_HEADER);

        assertEquals(1L, response.getId());
        assertEquals("CustomerTest", response.getCustomerName());
        assertEquals(1, response.getItems().size());
        assertEquals(1L, response.getItems().get(0).getProductId());
        assertEquals("ProductA", response.getItems().get(0).getProductName());
        assertEquals(2, response.getItems().get(0).getQuantity());
    }

    @Test
    void deleteOrder_whenExist_thenShouldDelete() {
        when(userFeignInterface.validateUser(AUTH_HEADER)).thenReturn(ResponseEntity.ok().build());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrder(1L, AUTH_HEADER);

        verify(kafkaProducerService, times(1)).sendMessage(eq("order-events-update"), eq("DELETE"), any());
        verify(kafkaProducerService, times(1)).sendMessage(eq("order-events"), eq("DELETE"), eq("Order ID: 1"));
        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    void deleteOrder_whenNotExist_thenShouldNotDelete() {
        when(userFeignInterface.validateUser(AUTH_HEADER)).thenReturn(ResponseEntity.ok().build());
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> orderService.deleteOrder(2L, AUTH_HEADER));

        verify(kafkaProducerService, never()).sendMessage(anyString(), anyString(), any());
        verify(orderRepository, never()).deleteById(anyLong());
    }


    @Test
    void getOrderByCustomerName_thenReturnOrder() {
        when(userFeignInterface.validateUserRole(AUTH_HEADER, "ADMIN")).thenReturn(true);

        when(orderRepository.findByCustomerName("CustomerTest")).thenReturn(List.of(order));

        List<OrderResponseDTO> result = orderService.getOrdersByCustomerName("CustomerTest", AUTH_HEADER);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CustomerTest", result.get(0).getCustomerName());
        assertEquals(1, result.get(0).getItems().size());

        verify(userFeignInterface, times(1)).validateUserRole(AUTH_HEADER, "ADMIN");
        verify(orderRepository, times(1)).findByCustomerName("CustomerTest");
    }

    @Test
    void updateOrder_whenValid_thenShouldUpdateOrder() {
        when(userFeignInterface.validateUser(AUTH_HEADER)).thenReturn(ResponseEntity.ok().build());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Map<Long, Boolean> stockAvailability = Map.of(1L, true);
        when(orderFeignInterface.checkStockAvailability(anyList())).thenReturn(stockAvailability);

        OrderUpdateDTO updateRequest = new OrderUpdateDTO(List.of(new OrderItemDTO(1L, "ProductA", 3)));

        OrderResponseDTO result = orderService.updateOrder(1L, updateRequest, AUTH_HEADER);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("CustomerTest", result.getCustomerName());
        assertEquals(1, result.getItems().size());
        assertEquals(3, result.getItems().get(0).getQuantity());

        verify(kafkaProducerService, times(1)).sendMessage(eq("order-events-update"), eq("UPDATE"),
                any(InventoryUpdateDTO.class));

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void deleteOrder_whenUserNotAuthorized_thenShouldNotDelete() {
        lenient().when(userFeignInterface.validateUser(null))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        assertThrows(ResponseStatusException.class, () -> orderService.deleteOrder(1L, null));

        verify(kafkaProducerService, never()).sendMessage(anyString(), anyString(), anyString());
        verify(orderRepository, never()).deleteById(anyLong());
    }

    @Test
    void getOrdersByCustomerName_whenUserNotAuthorized_thenShouldThrowException() {
        lenient().when(userFeignInterface.validateUserRole(null, "ADMIN"))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        assertThrows(ResponseStatusException.class, () -> orderService.getOrdersByCustomerName("CustomerTest", null));

        verify(orderRepository, never()).findByCustomerName(anyString());
    }

    @Test
    void createOrder_whenCustomerNameIsEmpty_thenShouldThrowException() {
        OrderRequestDTO request = new OrderRequestDTO("", List.of(new OrderItemDTO(1L, "Product", 2)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(request, AUTH_HEADER));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Имя пользователя не может быть пустым", exception.getReason());
    }

    @Test
    void createOrder_whenItemsListIsEmpty_thenShouldThrowException() {
        OrderRequestDTO request = new OrderRequestDTO("Customer", List.of());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(request, AUTH_HEADER));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Список товаров не может быть пустым", exception.getReason());
    }
}

