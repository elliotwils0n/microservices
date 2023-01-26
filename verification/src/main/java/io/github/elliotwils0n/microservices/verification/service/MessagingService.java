package io.github.elliotwils0n.microservices.verification.service;

import io.github.elliotwils0n.microservices.core.event.VerificationEvent;

public interface MessagingService {

    void sendMessage(VerificationEvent message);

}
