package io.github.elliotwils0n.microservices.user.config;

import io.github.elliotwils0n.microservices.user.UserApp;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { UserApp.class })
@TestConfiguration
@Import({ TestChannelBinderConfiguration.class })
@EnableAutoConfiguration(exclude = { LiquibaseAutoConfiguration.class })
public @interface UserTestConfiguration {
}
