package io.github.elliotwils0n.microservices.user.controller;

import io.github.elliotwils0n.microservices.core.model.ServerMessage;
import io.github.elliotwils0n.microservices.user.model.UserCredentials;
import io.github.elliotwils0n.microservices.user.model.UserRegistrationRequest;
import io.github.elliotwils0n.microservices.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ServerMessage> registerUser(@RequestBody UserRegistrationRequest userRegistrationRequest) {
        ServerMessage response = userService.register(userRegistrationRequest);
        return new ResponseEntity<ServerMessage>(response, HttpStatus.valueOf(response.getCode()));
    }

    @PostMapping("/checkStatus")
    public ResponseEntity<ServerMessage> checkUserStatus(@RequestBody UserCredentials userCredentials) {
        ServerMessage response = userService.getAccountStatus(userCredentials);
        return new ResponseEntity<ServerMessage>(response, HttpStatus.valueOf(response.getCode()));
    }

}
