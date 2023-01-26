package io.github.elliotwils0n.microservices.verification.messaging;

import io.github.elliotwils0n.microservices.core.event.UserEvent;
import io.github.elliotwils0n.microservices.verification.service.VerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Slf4j
public class UserMessageConsumer {

    @Autowired
    private VerificationService verificationService;

    @Bean
    public Consumer<UserEvent> userEventSink() {
        return verificationService::handleMessage;
    }

}
