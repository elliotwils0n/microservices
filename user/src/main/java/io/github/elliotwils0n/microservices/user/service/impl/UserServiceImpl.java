package io.github.elliotwils0n.microservices.user.service.impl;

import io.github.elliotwils0n.microservices.core.enumeration.UserEventType;
import io.github.elliotwils0n.microservices.core.enumeration.VerificationEventType;
import io.github.elliotwils0n.microservices.core.event.UserEvent;
import io.github.elliotwils0n.microservices.core.event.VerificationEvent;
import io.github.elliotwils0n.microservices.core.model.ServerMessage;
import io.github.elliotwils0n.microservices.user.entity.User;
import io.github.elliotwils0n.microservices.user.mapper.UserRegistrationRequestToUserMapper;
import io.github.elliotwils0n.microservices.user.model.UserCredentials;
import io.github.elliotwils0n.microservices.user.model.UserRegistrationRequest;
import io.github.elliotwils0n.microservices.user.repository.UserRepository;
import io.github.elliotwils0n.microservices.user.service.MessagingService;
import io.github.elliotwils0n.microservices.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final MessagingService messagingService;
    private final UserRepository userRepository;
    private final UserRegistrationRequestToUserMapper userRegistrationRequestToUserMapper;
    private final PasswordEncoder passwordEncoder;

    public ServerMessage register(UserRegistrationRequest userRegistrationRequest) {
        if (!IS_REQUEST_VALID.test(userRegistrationRequest)) {
            return new ServerMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "Invalid request. Required valid fields: username, email, password.");
        }

        long emailCount = userRepository.countByEmail(userRegistrationRequest.getEmail());
        long usernameCount = userRepository.countByUsername(userRegistrationRequest.getUsername());

        if (emailCount != 0) {
            return new ServerMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    String.format("Email %s already registered.", userRegistrationRequest.getEmail()));
        }

        if (usernameCount != 0) {
            return new ServerMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    String.format("Username %s already taken.", userRegistrationRequest.getUsername()));
        }

        User user = userRepository.save(userRegistrationRequestToUserMapper.apply(userRegistrationRequest));
        log.info("User \"{}\" registered successfully.", user.getId());

        UserEvent event = new UserEvent(UserEventType.USER_CREATED, user.getId());
        messagingService.sendMessage(event);

        return new ServerMessage(
                HttpStatus.CREATED.value(), HttpStatus.CREATED.getReasonPhrase(),
                String.format("User %s registered successfully", user.getId()));
    }

    @Override
    public ServerMessage getAccountStatus(UserCredentials userCredentials) {
        Optional<User> userOptional = userRepository.findByEmail(userCredentials.getEmail());
        if (userOptional.isEmpty() || !passwordEncoder.matches(userCredentials.getPassword(), userOptional.get().getPasswordHash())) {
                return new ServerMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "There is no match in user database for given email and/or password.");
        }
        return new ServerMessage(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                String.format("Account active status: %s", userOptional.get().getActive()));
    }

    @Override
    public void handleMessage(VerificationEvent event) {
        log.debug("Message received: NotificationEvent {}", event.getType());

        if (event.getType() != VerificationEventType.EMAIL_VERIFIED) {
            throw new UnsupportedOperationException(String.format("Notification Event of Type %s not supported!", event.getType()));
        }
        userRepository.findById(event.getUserId()).ifPresentOrElse(
                user -> {
                    user.setActive(Boolean.TRUE);
                    userRepository.save(user);
                    log.debug("User account {} has been activated.", user.getId());
                    },
                () -> log.debug("User {} does not exist.", event.getUserId())
        );
    }

    private final Predicate<UserRegistrationRequest> IS_REQUEST_VALID =
            userRegistrationRequest ->
                    StringUtils.isNoneBlank(
                            userRegistrationRequest.getUsername(),
                            userRegistrationRequest.getEmail(),
                            userRegistrationRequest.getPassword())
                    && EmailValidator.getInstance().isValid(userRegistrationRequest.getEmail());

}
