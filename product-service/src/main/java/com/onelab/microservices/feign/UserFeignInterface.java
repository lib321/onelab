package com.onelab.microservices.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "USER-SERVICE", url = "http://localhost:8084")
public interface UserFeignInterface {

    @GetMapping("/auth/validate-role")
    Boolean validateUserRole(@RequestHeader("Authorization") String authHeader,
                             @RequestParam("role") String role);
}
