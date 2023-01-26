package io.github.elliotwils0n.microservices.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.elliotwils0n.microservices.core.enumeration.UserEventType;
import io.github.elliotwils0n.microservices.core.event.UserEvent;
import io.github.elliotwils0n.microservices.user.config.UserTestConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.messaging.Message;

import java.io.IOException;
import java.util.UUID;

@UserTestConfiguration
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
        UserEvent userEvent = new UserEvent();
        userEvent.setType(UserEventType.USER_CREATED);
        userEvent.setUserId(UUID.randomUUID());

        // when
        messagingService.sendMessage(userEvent);
        Message<byte[]> message = outputDestination.receive(1, "test/user-event");

        // then
        Assertions.assertNotNull(message);
        UserEvent sentUserEvent = new ObjectMapper().readValue(message.getPayload(), UserEvent.class);
        Assertions.assertEquals(userEvent.getType(), sentUserEvent.getType());
        Assertions.assertEquals(userEvent.getUserId(), sentUserEvent.getUserId());
    }

}
