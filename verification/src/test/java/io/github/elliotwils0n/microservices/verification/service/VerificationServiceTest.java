package io.github.elliotwils0n.microservices.verification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.elliotwils0n.microservices.core.enumeration.UserEventType;
import io.github.elliotwils0n.microservices.core.enumeration.VerificationEventType;
import io.github.elliotwils0n.microservices.core.event.UserEvent;
import io.github.elliotwils0n.microservices.core.event.VerificationEvent;
import io.github.elliotwils0n.microservices.core.model.ServerMessage;
import io.github.elliotwils0n.microservices.verification.config.VerificationTestConfiguration;
import io.github.elliotwils0n.microservices.verification.entity.Verification;
import io.github.elliotwils0n.microservices.verification.repository.VerificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@VerificationTestConfiguration
class VerificationServiceTest {

    @Autowired
    private VerificationService verificationService;

    @SpyBean
    private VerificationRepository verificationRepository;

    @Autowired
    private OutputDestination outputDestination;

    @SpyBean
    private MessagingService messagingService;

    private final UUID DEFAULT_UUID = UUID.randomUUID();

    private final String DEFAULT_HASH = "DEFAULT_HASH";

    @BeforeEach
    void init() {
        verificationRepository.deleteAll();
        doNothing().when(messagingService).sendMessage(any());
    }

    @AfterEach
    void clean() {
        outputDestination.clear();
    }

    @Test
    void shouldSaveVerification() {
        // given
        UserEvent userEvent = new UserEvent(UserEventType.USER_CREATED, UUID.randomUUID());

        // when
        verificationService.handleMessage(userEvent);
        List<Verification> verifications = verificationRepository.findAll();

        // then
        Assertions.assertEquals(1, verifications.size());
        Assertions.assertNotNull(verifications.get(0).getId());
        Assertions.assertNotNull(verifications.get(0).getHash());
        Assertions.assertEquals(userEvent.getUserId(), verifications.get(0).getUserId());
        Assertions.assertEquals(Boolean.FALSE, verifications.get(0).getUsed());
    }

    @ParameterizedTest
    @ValueSource(strings = { "USER_UPDATED", "USER_DELETED" })
    void shouldThrowUnsupportedOperationExceptionWhenUserEventTypeNotSupported(String userEventType) {
        // given
        UserEvent userEvent = new UserEvent(UserEventType.valueOf(userEventType), UUID.randomUUID());

        // when - then
        Assertions.assertThrows(UnsupportedOperationException.class, () -> verificationService.handleMessage(userEvent));
    }

    @Test
    void shouldVerifyUserWhenValidUserIdAndHashProvided() {
        // given
        verificationRepository.save(getVerification());

        // when
        ServerMessage serverMessage = verificationService.verify(DEFAULT_UUID, DEFAULT_HASH);

        // then
        assertMessage(serverMessage, "Verification success. Account should be verified soon.", HttpStatus.OK);
        Mockito.verify(messagingService, times(1)).sendMessage(any());
    }

    @Test
    void shouldSendMessageToExchangeAfterVerification() throws IOException {
        // given
        doCallRealMethod().when(messagingService).sendMessage(any());

        verificationRepository.save(getVerification());

        // when
        ServerMessage serverMessage = verificationService.verify(DEFAULT_UUID, DEFAULT_HASH);
        Message<byte[]> message = outputDestination.receive(1, "test/verification-event");

        // then
        assertMessage(serverMessage, "Verification success. Account should be verified soon.", HttpStatus.OK);
        Mockito.verify(messagingService, times(1)).sendMessage(any());

        Assertions.assertNotNull(message);
        VerificationEvent verificationEvent = new ObjectMapper().readValue(message.getPayload(), VerificationEvent.class);
        Assertions.assertEquals(VerificationEventType.EMAIL_VERIFIED, verificationEvent.getType());
        Assertions.assertEquals(DEFAULT_UUID, verificationEvent.getUserId());
    }

    @Test
    void shouldNotVerifyUserWhenInvalidHashProvided() {
        // given
        verificationRepository.save(getVerification());

        // when
        ServerMessage serverMessage = verificationService.verify(DEFAULT_UUID, "invalidHash");

        // then
        assertMessage(serverMessage, "Invalid combination of username and hash or link expired.", HttpStatus.BAD_REQUEST);
        Mockito.verify(messagingService, never()).sendMessage(any());
    }

    @Test
    void shouldNotVerifyUserWhenInvalidUserProvided() {
        // given
        verificationRepository.save(getVerification());

        // when
        ServerMessage serverMessage = verificationService.verify(UUID.randomUUID(), DEFAULT_HASH);

        // then
        assertMessage(serverMessage, "Invalid combination of username and hash or link expired.", HttpStatus.BAD_REQUEST);
        Mockito.verify(messagingService, never()).sendMessage(any());
    }

    @Test
    void shouldNotVerifyUserWhenAlreadyUsed() {
        // given
        Verification verification = getVerification();
        verification.setUsed(Boolean.TRUE);
        verificationRepository.save(verification);

        // when
        ServerMessage serverMessage = verificationService.verify(DEFAULT_UUID, DEFAULT_HASH);

        // then
        assertMessage(serverMessage, "Invalid combination of username and hash or link expired.", HttpStatus.BAD_REQUEST);
        Mockito.verify(messagingService, never()).sendMessage(any());
    }

    private void assertMessage(ServerMessage serverMessage, String expectedMessage, HttpStatus expectedHttpStatus) {
        Assertions.assertEquals(expectedMessage, serverMessage.getMessage());
        Assertions.assertEquals(expectedHttpStatus.getReasonPhrase(), serverMessage.getStatus());
        Assertions.assertEquals(expectedHttpStatus.value(), serverMessage.getCode());
    }

    private Verification getVerification() {
        Verification verification = new Verification();
        verification.setUserId(DEFAULT_UUID);
        verification.setHash(DEFAULT_HASH);
        verification.setUsed(Boolean.FALSE);

        return verification;
    }

}
