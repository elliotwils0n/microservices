package io.github.elliotwils0n.microservices.user.service.impl;

import io.github.elliotwils0n.microservices.core.event.UserEvent;
import io.github.elliotwils0n.microservices.user.service.MessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessagingServiceImpl implements MessagingService {

    private final StreamBridge streamBridge;

    private final String destination = "user-event";

    @Value("${microservices.environment}")
    private String environment;

    @Override
    public void sendMessage(UserEvent message) {
        String finalDestination = String.format("%s/%s", environment, destination);
        boolean sent = streamBridge.send(finalDestination, message);
        log.debug("Message {} sent to {}. Message Content: \n{}", sent ? "" : "NOT", finalDestination, message);
    }

}
