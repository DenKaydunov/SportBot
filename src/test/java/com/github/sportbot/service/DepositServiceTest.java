package com.github.sportbot.service;

import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DepositServiceTest {

    @InjectMocks
    private DepositService depositService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageSource messageSource;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setTelegramId(100L);
        user.setLanguage("ru");
        user.setBalanceTon(0);
    }

    @Test
    void currentBalanceTon() {
        Locale locale = Locale.forLanguageTag("ru");
        user.setBalanceTon(10);

        when(userService.getUserLocale(user)).thenReturn(locale);
        when(messageSource.getMessage(
                eq("balance.ton"),
                any(),
                any(Locale.class))
        ).thenReturn("Ваш баланс 10 ТОН.");

        String result = depositService.currentBalanceTon(user);

        assertEquals("Ваш баланс 10 ТОН.", result);
    }

    @Test
    void depositBalance() {
        Long telegramId = 100L;
        Integer plusTon = 10;
        Locale locale = Locale.forLanguageTag("ru");
        user.setBalanceTon(10);

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);
        when(userService.getUserLocale(user)).thenReturn(locale);
        when(messageSource.getMessage(
                eq("balance.ton.deposit"),
                any(),
                any(Locale.class))
        ).thenReturn("Ваш баланс пополнен на 10 ТОН.");
        when(messageSource.getMessage(
                eq("balance.ton"),
                any(),
                any(Locale.class))
        ).thenReturn("Ваш баланс 20 ТОН.");

        String result = depositService.depositBalance(telegramId, plusTon);

        assertEquals(20, user.getBalanceTon());
        verify(userRepository).save(user);
        assertEquals("Ваш баланс пополнен на 10 ТОН.Ваш баланс 20 ТОН.", result);


    }

}
