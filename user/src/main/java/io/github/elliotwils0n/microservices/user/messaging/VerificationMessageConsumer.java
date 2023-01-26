package io.github.elliotwils0n.microservices.user.messaging;

import io.github.elliotwils0n.microservices.core.event.VerificationEvent;
import io.github.elliotwils0n.microservices.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerificationMessageConsumer {

    private final UserService userService;

    @Bean
    public Consumer<VerificationEvent> verificationEventSink() {
        return userService::handleMessage;
    }

}
