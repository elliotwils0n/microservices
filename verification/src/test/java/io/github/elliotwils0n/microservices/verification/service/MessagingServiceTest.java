package io.github.elliotwils0n.microservices.verification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.elliotwils0n.microservices.core.enumeration.VerificationEventType;
import io.github.elliotwils0n.microservices.core.event.VerificationEvent;
import io.github.elliotwils0n.microservices.verification.config.VerificationTestConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.messaging.Message;

import java.io.IOException;
import java.util.UUID;

@VerificationTestConfiguration
class MessagingServiceTest {

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private OutputDestination outputDestination;

    @AfterEach
    void clean() {
        outputDestination.clear();
    }

    @Test
    void shouldSendMessageToExchange() throws IOException {
        // given
        VerificationEvent verificationEvent = new VerificationEvent();
        verificationEvent.setType(VerificationEventType.EMAIL_VERIFIED);
        verificationEvent.setUserId(UUID.randomUUID());

        // when
        messagingService.sendMessage(verificationEvent);
        Message<byte[]> message = outputDestination.receive(1, "test/verification-event");

        // then
        Assertions.assertNotNull(message);
        VerificationEvent sentVerificationEvent = new ObjectMapper().readValue(message.getPayload(), VerificationEvent.class);
        Assertions.assertEquals(verificationEvent.getType(), sentVerificationEvent.getType());
        Assertions.assertEquals(verificationEvent.getUserId(), sentVerificationEvent.getUserId());
    }

}