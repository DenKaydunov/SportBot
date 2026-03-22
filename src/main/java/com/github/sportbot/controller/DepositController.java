package com.github.sportbot.controller;

import com.github.sportbot.model.User;
import com.github.sportbot.service.DepositService;
import com.github.sportbot.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/balance")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;
    private final UserService userService;

    @PostMapping("/deposit")
    public String depositBalance(@RequestParam
                                 @Parameter(example = "1000001")
                                 @NotNull
                                 Long telegramId,

                                 @NotNull
                                 Integer depositTon){
        return depositService.depositBalanceTon(telegramId, depositTon);
    }

    @GetMapping("/current")
    public  String getBalance(@RequestParam
                              @Parameter(example = "1000001")
                              @NotNull
                              Long telegramId) {
        User user = userService.getUserByTelegramId(telegramId);
        return depositService.currentBalanceTon(user);
    }
}
