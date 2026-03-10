package com.github.sportbot.controller;

import com.github.sportbot.dto.RegistrationRequest;
import com.github.sportbot.dto.UserResponse;
import com.github.sportbot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserResponse registerUser(@RequestBody RegistrationRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("/unsubscribe/{telegramId}")
    public String unsubscribeUser(@PathVariable Long telegramId){
        return userService.unsubscribeUser(telegramId);
    }
}
