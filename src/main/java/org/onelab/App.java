package org.onelab;


import org.onelab.config.AppConfig;
import org.onelab.dto.Order;
import org.onelab.dto.Product;
import org.onelab.dto.User;
import org.onelab.service.AppService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.*;


public class App {
    public static void main(String[] args) {
        User user = User.builder()
                .id(1)
                .firstname("Test")
                .lastname("Test")
                .orders(new HashSet<>())
                .build();

        Product product1 = Product.builder()
                .id(1)
                .name("Laptop")
                .price(10000)
                .build();

        Product product2 = Product.builder()
                .id(2)
                .name("Desktop")
                .price(20000)
                .build();

        Product product3 = Product.builder()
                .id(3)
                .name("Headphone")
                .price(5000)
                .build();

        Product product4 = Product.builder()
                .id(4)
                .name("Keyboard")
                .price(7500)
                .build();

        Order order = Order.builder()
                .id(1)
                .name("order 1")
                .products(new ArrayList<>())
                .build();

        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        AppService appService = context.getBean(AppService.class);

        appService.addProduct(product1);
        appService.addProduct(product2);
        appService.addProduct(product3);
        appService.addProduct(product4);

        Scanner scanner = new Scanner(System.in);
        boolean b = false;
        while (!b) {
            System.out.println("Веберите товар:");
            for (Product product : appService.getProducts()) {
                System.out.println("- " + product.getName() + " [" + product.getId() + "]");
            }
            String str1 = scanner.nextLine();

            for (Product product : appService.getProducts()) {
                if (Integer.parseInt(str1) == product.getId()) {
                    appService.addProductToOrder(product, order);
                    appService.addOrderToUser(order, user);
                }
            }
            System.out.println("Введите 'stop' или нажмите enter");

            String str2 = scanner.nextLine();
            if (str2.equals("stop")) {
                b = true;
            }
        }

        for (Order userOrder : user.getOrders()) {
            System.out.println(userOrder.getName() + " [" + order.getId() + "]");
            for (Product product : userOrder.getProducts()) {
                System.out.println("- " + product.getName() + " [" + product.getId() + "]");
            }
        }

        System.out.println("Для удаления введите id заказа и товара в формате 'order id, product id'");
        String str = scanner.nextLine();

        String[] split = str.split(",\\s*");

        Order currentOrder = appService.getOrderFromUser(user.getId(), Integer.parseInt(split[0]));
        Optional<Product> deletedProduct = appService.getProductById(Integer.parseInt(split[1]));

        appService.deleteProductFromOrder(currentOrder, user, deletedProduct.get());

        for (Order userOrder : user.getOrders()) {
            System.out.println(userOrder.getName() + " [" + order.getId() + "]");
            for (Product product : userOrder.getProducts()) {
                System.out.println("- " + product.getName() + " [" + product.getId() + "]");
            }
        }
    }
}
