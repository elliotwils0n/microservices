package io.github.elliotwils0n.microservices.verification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class VerificationApp {

    public static void main( String[] args ) {
        SpringApplication.run(VerificationApp.class, args);
    }

}
