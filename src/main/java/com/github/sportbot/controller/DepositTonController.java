package com.github.sportbot.controller;

import com.github.sportbot.model.User;
import com.github.sportbot.service.DepositTonService;
import com.github.sportbot.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ton")
@RequiredArgsConstructor
public class DepositTonController {

    private final DepositTonService depositTonService;
    private final UserService userService;

    @PostMapping("/deposit")
    public String depositBalanceTon(@RequestParam
                                    @Parameter(example = "1000001")
                                    @NotNull
                                    Long telegramId,

                                    @RequestParam
                                    @Parameter(example = "1000001")
                                    @NotNull
                                    Integer depositTon){
        return depositTonService.depositBalanceTon(telegramId, depositTon);
    }

    @GetMapping("/current")
    public  String currentBalanceTon(@RequestParam
                                         @Parameter(example = "1000001")
                                         @NotNull
                                         Long telegramId) {
        User user = userService.getUserByTelegramId(telegramId);
        return depositTonService.currentBalanceTon(user);
    }
}
