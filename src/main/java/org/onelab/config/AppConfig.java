package org.onelab.config;

import org.onelab.repoimpl.OrderRepoImpl;
import org.onelab.repoimpl.ProductRepoImpl;
import org.onelab.repoimpl.UserRepoImpl;
import org.onelab.service.AppService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public OrderRepoImpl orderRepo() {
        return new OrderRepoImpl();
    }

    @Bean
    public ProductRepoImpl productRepo() {
        return new ProductRepoImpl();
    }

    @Bean
    public UserRepoImpl userRepo() {
        return new UserRepoImpl();
    }

    @Bean
    public AppService service() {
        return new AppService(orderRepo(), productRepo(), userRepo());
    }
}
