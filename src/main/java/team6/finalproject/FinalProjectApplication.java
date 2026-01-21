package team6.finalproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;

@SpringBootApplication(exclude = {S3AutoConfiguration.class})
@EnableFeignClients
@EnableScheduling
@EnableJpaAuditing
public class FinalProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinalProjectApplication.class, args);
    }
}
