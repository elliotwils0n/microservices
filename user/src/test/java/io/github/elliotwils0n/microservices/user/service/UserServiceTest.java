package io.github.elliotwils0n.microservices.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.elliotwils0n.microservices.core.enumeration.UserEventType;
import io.github.elliotwils0n.microservices.core.enumeration.VerificationEventType;
import io.github.elliotwils0n.microservices.core.event.UserEvent;
import io.github.elliotwils0n.microservices.core.event.VerificationEvent;
import io.github.elliotwils0n.microservices.core.model.ServerMessage;
import io.github.elliotwils0n.microservices.user.config.UserTestConfiguration;
import io.github.elliotwils0n.microservices.user.entity.User;
import io.github.elliotwils0n.microservices.user.model.UserCredentials;
import io.github.elliotwils0n.microservices.user.model.UserRegistrationRequest;
import io.github.elliotwils0n.microservices.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.mockito.Mockito.verify;

@UserTestConfiguration
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OutputDestination outputDestination;

    @SpyBean
    private MessagingService messagingService;

    private final UUID DEFAULT_UUID = UUID.randomUUID();

    private static final String DEFAULT_USERNAME = "username";

    private static final String DEFAULT_EMAIL = "username@site.com";

    private final String DEFAULT_PASSWORD = "password";


    @BeforeEach
    void init() {
        userRepository.deleteAll();
        doNothing().when(messagingService).sendMessage(any());
    }

    @AfterEach
    void clean() {
        outputDestination.clear();
    }

    @Test
    void shouldRegisterUserWhenEmailAndUsernameNotTaken() {
        // given
        UserRegistrationRequest registrationRequest =
                new UserRegistrationRequest(DEFAULT_USERNAME, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // when
        ServerMessage message = userService.register(registrationRequest);
        List<User> users = userRepository.findAll();

        // then
        Assertions.assertEquals(1, users.size());
        assertMessage(message, String.format("User %s registered successfully", users.get(0).getId()), HttpStatus.CREATED);
        verify(messagingService, times(1)).sendMessage(any());
    }

    @Test
    void shouldSendMessageToExchangeAfterUserRegistration() throws IOException {
        // given
        doCallRealMethod().when(messagingService).sendMessage(any());
        UserRegistrationRequest registrationRequest =
                new UserRegistrationRequest(DEFAULT_USERNAME, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // when
        userService.register(registrationRequest);
        Message<byte[]> message = outputDestination.receive(1, "test/user-event");

        // then
        verify(messagingService, times(1)).sendMessage(any());
        Assertions.assertNotNull(message);

        UserEvent userEvent = new ObjectMapper().readValue(message.getPayload(), UserEvent.class);
        Assertions.assertEquals(UserEventType.USER_CREATED, userEvent.getType());
        Assertions.assertNotNull(userEvent.getUserId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " " })
    void shouldNotRegisterUserWhenBlankUsername(String username) {
        // given
        UserRegistrationRequest registrationRequest =
                new UserRegistrationRequest(username, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // when
        ServerMessage message = userService.register(registrationRequest);

        // then
        assertMessage(message, "Invalid request. Required valid fields: username, email, password.", HttpStatus.BAD_REQUEST);
        verify(messagingService, never()).sendMessage(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "invalid@email@address.com", "invalid@address", "invalid", "invalid@" })
    void shouldNotRegisterUserWhenBlankOrInvalidEmail(String email) {
        // given
        UserRegistrationRequest registrationRequest =
                new UserRegistrationRequest(DEFAULT_USERNAME, email, DEFAULT_PASSWORD);

        // when
        ServerMessage message = userService.register(registrationRequest);

        // then
        assertMessage(message, "Invalid request. Required valid fields: username, email, password.", HttpStatus.BAD_REQUEST);
        verify(messagingService, never()).sendMessage(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " " })
    void shouldNotRegisterUserWhenBlankPassword(String password) {
        // given
        UserRegistrationRequest registrationRequest =
                new UserRegistrationRequest(DEFAULT_USERNAME, DEFAULT_EMAIL, password);

        // when
        ServerMessage message = userService.register(registrationRequest);

        // then
        assertMessage(message, "Invalid request. Required valid fields: username, email, password.", HttpStatus.BAD_REQUEST);
        verify(messagingService, never()).sendMessage(any());
    }

    @Test
    void shouldNotRegisterUserWhenEmailIsTaken() {
        // given
        User user = getUser();
        user.setUsername("NON_DEFAULT_USERNAME");
        userRepository.save(user);
        UserRegistrationRequest registrationRequest =
                new UserRegistrationRequest(DEFAULT_USERNAME, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // when
        ServerMessage message = userService.register(registrationRequest);
        long userCount = userRepository.count();

        // then
        Assertions.assertEquals(1L, userCount);
        assertMessage(message, String.format("Email %s already registered.", DEFAULT_EMAIL), HttpStatus.BAD_REQUEST);
        verify(messagingService, never()).sendMessage(any());
    }

    @Test
    void shouldNotRegisterUserWhenUsernameIsTaken() {
        // given
        User user = getUser();
        user.setEmail("NON_DEFAULT_EMAIL@site.com");
        userRepository.save(user);
        UserRegistrationRequest registrationRequest =
                new UserRegistrationRequest(DEFAULT_USERNAME, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // when
        ServerMessage message = userService.register(registrationRequest);
        long userCount = userRepository.count();

        // then
        Assertions.assertEquals(1L, userCount);
        assertMessage(message, String.format("Username %s already taken.", DEFAULT_USERNAME), HttpStatus.BAD_REQUEST);
        verify(messagingService, never()).sendMessage(any());
    }

    @Test
    void shouldReturnAccountStatusWhenCorrectUserCredentials() {
        // given
        userRepository.save(getUser());
        UserCredentials userCredentials = new UserCredentials(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // when
        ServerMessage message = userService.getAccountStatus(userCredentials);

        // then
        assertMessage(message, "Account active status: false", HttpStatus.OK);
    }

    @Test
    void shouldNotReturnAccountStatusWhenInvalidUserCredentials() {
        // given
        userRepository.save(getUser());
        UserCredentials userCredentials = new UserCredentials(DEFAULT_EMAIL, "invalidPassword");

        // when
        ServerMessage message = userService.getAccountStatus(userCredentials);

        // then
        assertMessage(message, "There is no match in user database for given email and/or password.", HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotReturnAccountStatusWhenUserDoesNotExist() {
        // given
        UserCredentials userCredentials = new UserCredentials(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // when
        ServerMessage message = userService.getAccountStatus(userCredentials);

        // then
        assertMessage(message, "There is no match in user database for given email and/or password.", HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldActivateUserAccount() {
        // given
        User user = userRepository.save(getUser());
        VerificationEvent verificationEvent = new VerificationEvent(VerificationEventType.EMAIL_VERIFIED, user.getId());
        Boolean oldStatus = user.getActive();

        // when
        userService.handleMessage(verificationEvent);
        List<User> users = userRepository.findAll();

        // then
        Assertions.assertFalse(oldStatus);
        Assertions.assertEquals(1L, users.size());
        Assertions.assertTrue(users.get(0).getActive());
    }

    private void assertMessage(ServerMessage serverMessage, String expectedMessage, HttpStatus expectedHttpStatus) {
        Assertions.assertEquals(expectedMessage, serverMessage.getMessage());
        Assertions.assertEquals(expectedHttpStatus.getReasonPhrase(), serverMessage.getStatus());
        Assertions.assertEquals(expectedHttpStatus.value(), serverMessage.getCode());
    }

    private User getUser() {
        User user = new User();
        user.setUsername(DEFAULT_USERNAME);
        user.setEmail(DEFAULT_EMAIL);
        user.setActive(Boolean.FALSE);
        user.setPasswordHash("$2a$12$FsSuvpX/K1g5NtidjwMsa.9mBG6mSWw.YbdYlllXHJiA6b.DRK95S"); // raw: password
        return user;
    }

}
