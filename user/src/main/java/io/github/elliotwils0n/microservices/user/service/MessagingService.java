package io.github.elliotwils0n.microservices.user.service;

import io.github.elliotwils0n.microservices.core.event.UserEvent;

public interface MessagingService {

    void sendMessage(UserEvent message);

}
