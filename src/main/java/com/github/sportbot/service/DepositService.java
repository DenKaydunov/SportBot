package com.github.sportbot.service;

import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DepositService implements MessageLocalizer {

    private final UserService userService;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public String depositBalance(Long telegramID,
                                 Integer plusValue) {
        User user = userService.getUserByTelegramId(telegramID);
        Locale locale = userService.getUserLocale(user);

        Integer currentBalance = user.getBalanceTon();
        Integer sum = currentBalance + plusValue;

        user.setBalanceTon(sum);
        userRepository.save(user);

        String depositMessage = localize("balance.ton.deposit",
                new Object[] {plusValue}, locale);

        String updatedBalance = currentBalanceTon(user);

        return depositMessage + updatedBalance;
    }

    public String currentBalanceTon(User user) {
        Integer currentBalance = user.getBalanceTon();
        Locale locale = userService.getUserLocale(user);

        return localize("balance.ton",
                new Object[]{currentBalance}, locale);
    }

    @Override
    public String localize(String messageKey, Object[] context, Locale locale) {
        return  messageSource.getMessage(
                messageKey,
                context,
                locale);
    }
}
