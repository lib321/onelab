package com.onelab.microservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelab.microservices.dto.*;
import com.onelab.microservices.event.KafkaProducerService;
import com.onelab.microservices.feign.OrderFeignInterface;
import com.onelab.microservices.feign.UserFeignInterface;
import com.onelab.microservices.service.OrderService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerTest {


    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private KafkaProducerService kafkaProducerService;

    @MockitoBean
    private UserFeignInterface userFeignInterface;

    @MockitoBean
    private OrderFeignInterface orderFeignInterface;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderResponseDTO responseDTO;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @BeforeEach
    void setUp() {
        responseDTO = new OrderResponseDTO(1L, "CustomerTest", List.of(
                new OrderItemDTO(1L, "ProductA", 2),
                new OrderItemDTO(2L, "ProductB", 3)
        ));
    }

    @Test
    void createOrderTest() throws Exception {
        OrderRequestDTO requestDTO = new OrderRequestDTO("CustomerTest", List.of(
                new OrderItemDTO(1L, "ProductA", 2),
                new OrderItemDTO(2L, "ProductB", 3))
        );

        Mockito.when(userFeignInterface.validateUser(Mockito.anyString())).thenReturn(ResponseEntity.ok().build());
        Mockito.when(orderService.createOrder(Mockito.any(OrderRequestDTO.class), Mockito.anyString()))
                .thenReturn(responseDTO);


        mockMvc.perform(post("/api/orders/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerName").value("CustomerTest"))
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void getOrderByIdTest() throws Exception {
        Mockito.when(userFeignInterface.validateUser(Mockito.anyString())).thenReturn(ResponseEntity.ok().build());
        Mockito.when(orderService.getOrder(Mockito.anyLong(), Mockito.anyString())).thenReturn(responseDTO);

        mockMvc.perform(get("/api/orders/get/{id}", 1L)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerName").value("CustomerTest"))
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void deleteOrderByIdTest() throws Exception {
        mockMvc.perform(delete("/api/orders/delete/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void getOrdersByCustomerNameTest() throws Exception {
        OrderResponseDTO responseDTO1 = new OrderResponseDTO(2L, "CustomerTest", List.of(
                new OrderItemDTO(3L, "ProductC", 4),
                new OrderItemDTO(4L, "ProductD", 5)
        ));

        Mockito.when(userFeignInterface.validateUserRole(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(orderService.getOrdersByCustomerName(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(List.of(responseDTO, responseDTO1));

        mockMvc.perform(get("/api/orders/all/{customerName}", "CustomerTest")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].customerName").value("CustomerTest"))
                .andExpect(jsonPath("$[0].items.length()").value(2))
                .andExpect(jsonPath("$[0].items[0].productId").value(1))
                .andExpect(jsonPath("$[0].items[0].productName").value("ProductA"))
                .andExpect(jsonPath("$[0].items[0].quantity").value(2))
                .andExpect(jsonPath("$[0].items[1].productId").value(2))
                .andExpect(jsonPath("$[0].items[1].productName").value("ProductB"))
                .andExpect(jsonPath("$[0].items[1].quantity").value(3))

                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].customerName").value("CustomerTest"))
                .andExpect(jsonPath("$[1].items.length()").value(2))
                .andExpect(jsonPath("$[1].items[0].productId").value(3))
                .andExpect(jsonPath("$[1].items[0].productName").value("ProductC"))
                .andExpect(jsonPath("$[1].items[0].quantity").value(4))
                .andExpect(jsonPath("$[1].items[1].productId").value(4))
                .andExpect(jsonPath("$[1].items[1].productName").value("ProductD"))
                .andExpect(jsonPath("$[1].items[1].quantity").value(5));
    }

    @Test
    void updateOrderTest() throws Exception {
        OrderResponseDTO updatedResponseDTO = new OrderResponseDTO(1L, "CustomerTest", List.of(
                new OrderItemDTO(1L, "ProductA", 1),
                new OrderItemDTO(2L, "ProductB", 4)
        ));

        Mockito.when(userFeignInterface.validateUser(Mockito.anyString())).thenReturn(ResponseEntity.ok().build());
        Mockito.when(orderService.updateOrder(Mockito.anyLong(), Mockito.any(OrderUpdateDTO.class), Mockito.anyString()))
                .thenReturn(updatedResponseDTO);

        mockMvc.perform(put("/api/orders/update/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(updatedResponseDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerName").value("CustomerTest"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].productId").value(1L))
                .andExpect(jsonPath("$.items[0].productName").value("ProductA"))
                .andExpect(jsonPath("$.items[0].quantity").value(1))
                .andExpect(jsonPath("$.items[1].productId").value(2L))
                .andExpect(jsonPath("$.items[1].productName").value("ProductB"))
                .andExpect(jsonPath("$.items[1].quantity").value(4));


    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }
}
