package io.github.elliotwils0n.microservices.user.messaging;

import io.github.elliotwils0n.microservices.core.enumeration.VerificationEventType;
import io.github.elliotwils0n.microservices.core.event.VerificationEvent;
import io.github.elliotwils0n.microservices.user.config.UserTestConfiguration;
import io.github.elliotwils0n.microservices.user.service.UserService;
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

@UserTestConfiguration
class VerificationMessageConsumerTest {

    @Autowired
    private InputDestination inputDestination;

    @Autowired
    private BindingService bindingService;

    @SpyBean
    private VerificationMessageConsumer verificationMessageConsumer;

    @MockBean
    private UserService userService;

    private String destination;

    @BeforeEach
    void init() {
        destination = bindingService
                .getBindingServiceProperties()
                .getBindingProperties(UserCloudStreamBindings.VERIFICATION_EVENT_CONSUMER)
                .getDestination();
    }

    @Test
    void shouldConsumeVerificationEvent() {
        // given
        VerificationEvent verificationEvent =
                new VerificationEvent(VerificationEventType.EMAIL_VERIFIED, UUID.randomUUID());

        // when
        inputDestination.send(new GenericMessage<>(verificationEvent), destination);

        // then
        verify(verificationMessageConsumer, times(1)).verificationEventSink();
        verify(userService, times(1)).handleMessage(verificationEvent);
    }

}
