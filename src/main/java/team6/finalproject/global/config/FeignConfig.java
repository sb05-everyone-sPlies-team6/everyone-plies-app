package team6.finalproject.global.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "team6.finalproject.domain.content.api")
public class FeignConfig {
}
