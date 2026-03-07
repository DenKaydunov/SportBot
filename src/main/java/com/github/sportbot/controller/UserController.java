package com.github.sportbot.controller;

import com.github.sportbot.dto.RegistrationRequest;
import com.github.sportbot.dto.UserRegistrationResponse;
import com.github.sportbot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserRegistrationResponse registerUser(@RequestBody RegistrationRequest request) {
        return userService.registerUser(request);
    }
}
