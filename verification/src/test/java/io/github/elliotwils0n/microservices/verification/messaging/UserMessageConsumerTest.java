package io.github.elliotwils0n.microservices.verification.messaging;

import io.github.elliotwils0n.microservices.core.enumeration.UserEventType;
import io.github.elliotwils0n.microservices.core.event.UserEvent;
import io.github.elliotwils0n.microservices.verification.config.VerificationTestConfiguration;
import io.github.elliotwils0n.microservices.verification.service.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.messaging.support.GenericMessage;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@VerificationTestConfiguration
class UserMessageConsumerTest {

    @Autowired
    private InputDestination inputDestination;

    @Autowired
    private BindingService bindingService;

    @SpyBean
    private UserMessageConsumer userMessageConsumer;

    @MockBean
    private VerificationService verificationService;

    private String destination;

    @BeforeEach
    void init() {
        destination = bindingService
                .getBindingServiceProperties()
                .getBindingProperties(VerificationStreamCloudBindings.USER_EVENT_CONSUMER)
                .getDestination();
    }

    @Test
    void shouldConsumeVerificationEvent() {
        // given
        UserEvent userEvent = new UserEvent(UserEventType.USER_CREATED, UUID.randomUUID());

        // when
        inputDestination.send(new GenericMessage<>(userEvent), destination);

        // then
        verify(userMessageConsumer, times(1)).userEventSink();
        verify(verificationService, times(1)).handleMessage(userEvent);
    }

}