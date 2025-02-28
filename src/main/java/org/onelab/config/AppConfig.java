package org.onelab.config;

import org.onelab.repoimpl.OrderRepoImpl;
import org.onelab.repoimpl.ProductRepoImpl;
import org.onelab.repoimpl.UserRepoImpl;
import org.onelab.service.AppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
public class AppConfig {

    private static Logger logger = LoggerFactory.getLogger(AppConfig.class);

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

    @Bean
    public DataSource dataSource() {
        try {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .addScript("classpath:jdbc/schema.sql")
                    .addScript("classpath:jdbc/test-data.sql").build();
        } catch (Exception e) {
            logger.error("Embedded datasource been cannot be created!", e);
            return null;
        }
    }
}
