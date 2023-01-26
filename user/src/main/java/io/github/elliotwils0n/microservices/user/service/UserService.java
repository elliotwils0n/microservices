package io.github.elliotwils0n.microservices.user.service;

import io.github.elliotwils0n.microservices.core.event.VerificationEvent;
import io.github.elliotwils0n.microservices.core.model.ServerMessage;
import io.github.elliotwils0n.microservices.user.model.UserCredentials;
import io.github.elliotwils0n.microservices.user.model.UserRegistrationRequest;

public interface UserService {

    ServerMessage register(UserRegistrationRequest registrationRequest);

    ServerMessage getAccountStatus(UserCredentials userCredentials);

    void handleMessage(VerificationEvent notificationEvent);

}
