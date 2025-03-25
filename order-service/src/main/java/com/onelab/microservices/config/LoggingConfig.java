package com.onelab.microservices.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelab.aop.ResponseRequestLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class LoggingConfig {
    @Bean
    public ResponseRequestLogger responseRequestLogger(ObjectMapper objectMapper) {
        return new ResponseRequestLogger(objectMapper);
    }
}
