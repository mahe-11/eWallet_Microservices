package com.app.userConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class UserRestConfig {
    @Bean
    RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
