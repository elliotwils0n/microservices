package io.github.elliotwils0n.microservices.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class UserApp {

    public static void main( String[] args ) {
        SpringApplication.run(UserApp.class, args);
    }

}
