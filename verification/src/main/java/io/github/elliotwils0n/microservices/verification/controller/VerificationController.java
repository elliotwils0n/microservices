package io.github.elliotwils0n.microservices.verification.controller;

import io.github.elliotwils0n.microservices.core.model.ServerMessage;
import io.github.elliotwils0n.microservices.verification.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/verification")
public class VerificationController {

    @Autowired
    private VerificationService verificationService;

    @GetMapping("/verify")
    public ResponseEntity<ServerMessage> verify(@RequestParam("user") UUID userId, @RequestParam("hash") String hash) {
        ServerMessage response = verificationService.verify(userId, hash);
        return new ResponseEntity<ServerMessage>(response, HttpStatus.valueOf(response.getCode()));
    }

}
