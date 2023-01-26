package io.github.elliotwils0n.microservices.verification.service;

import io.github.elliotwils0n.microservices.core.event.UserEvent;
import io.github.elliotwils0n.microservices.core.model.ServerMessage;

import java.util.UUID;

public interface VerificationService {

    void handleMessage(UserEvent event);

    ServerMessage verify(UUID userId, String hash);

}
