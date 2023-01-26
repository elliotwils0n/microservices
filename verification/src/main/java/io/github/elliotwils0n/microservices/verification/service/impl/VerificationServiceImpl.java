package io.github.elliotwils0n.microservices.verification.service.impl;

import io.github.elliotwils0n.microservices.core.enumeration.UserEventType;
import io.github.elliotwils0n.microservices.core.enumeration.VerificationEventType;
import io.github.elliotwils0n.microservices.core.event.UserEvent;
import io.github.elliotwils0n.microservices.core.event.VerificationEvent;
import io.github.elliotwils0n.microservices.core.model.ServerMessage;
import io.github.elliotwils0n.microservices.verification.entity.Verification;
import io.github.elliotwils0n.microservices.verification.mapper.UserEventToVerificationMapper;
import io.github.elliotwils0n.microservices.verification.repository.VerificationRepository;
import io.github.elliotwils0n.microservices.verification.service.MessagingService;
import io.github.elliotwils0n.microservices.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    private final VerificationRepository verificationRepository;
    private final MessagingService messagingService;

    @Override
    public void handleMessage(UserEvent event) {
        log.debug("Message received: UserEvent {}", event.getType());

        if(event.getType() != UserEventType.USER_CREATED) {
            throw new UnsupportedOperationException(String.format("User Event of Type %s not supported!", event.getType()));
        }

        Verification verification = UserEventToVerificationMapper.INSTANCE.apply(event);
        verificationRepository.save(verification);
        log.debug(String.format(
                "activation link: http://localhost:8080/service/verification/api/v1/verification/verify?user=%s&hash=%s",
                verification.getUserId(), verification.getHash()));
    }

    @Override
    public ServerMessage verify(UUID userId, String hash) {
        Optional<Verification> verificationOptional =
                verificationRepository.findByUserIdAndHashAndUsed(userId, hash, Boolean.FALSE);
        if (verificationOptional.isEmpty()) {
            return new ServerMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "Invalid combination of username and hash or link expired.");
        }
        updateVerificationUsage(verificationOptional.get());
        messagingService.sendMessage(new VerificationEvent(
                VerificationEventType.EMAIL_VERIFIED,
                verificationOptional.get().getUserId()));
        return new ServerMessage(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Verification success. Account should be verified soon.");
    }

    private void updateVerificationUsage(Verification verification) {
        verification.setUsed(true);
        verificationRepository.save(verification);
    }

}
