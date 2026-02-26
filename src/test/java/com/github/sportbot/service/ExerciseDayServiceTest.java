package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseDayRepository;
import com.github.sportbot.repository.ExerciseDaySummaryProjection;
import com.github.sportbot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExerciseDayServiceTest {

    @Mock
    private ExerciseDayRepository dayRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExerciseDayService dayService;

    private ExerciseEntryRequest testRequest;
    private User user;
    private LocalDate date;

    @BeforeEach
    void setUp(){
        testRequest = new ExerciseEntryRequest(123456L, "push_up", 10);
        date = LocalDate.of(2026, 2, 25);
        user = new User();
        user.setId(1);

    }

    @Test
    void getReportForDate_returnsFormattedReport(){
        //given
        when(userRepository.findByTelegramId(testRequest.telegramId())).thenReturn(Optional.of(user));

        when(dayRepository.getUserDayProgressBy(user, date))
                .thenReturn(List.of(
                        new ExerciseDaySummaryProjection() {
                            @Override
                            public String getTitle() {
                                return "Отжимания";
                            }
                            @Override
                            public Integer getTotalCount() {
                                return 10;
                            }
                        },
                        new ExerciseDaySummaryProjection() {
                            @Override
                            public String getTitle() {
                                return "Подтягивания";
                            }

                            @Override
                            public Integer getTotalCount() {
                                return 20;
                            }
                        }
                ));

        //when
        String result = dayService.progressForDay(testRequest, date);

        //then
        assertEquals("Твой прогресс за 25.02.2026\uD83D\uDCAA\uD83C\uDFFB:\nОтжимания - 10\nПодтягивания - 20\n", result);

    }


    @Test
    void getReportForDate_WhenNoExercise_ReturnOnlyHeader(){
        //given
        when(userRepository.findByTelegramId(testRequest.telegramId())).thenReturn(Optional.of(user));

        when(dayRepository.getUserDayProgressBy(user, date)).thenReturn(List.of());

        //when
        String result = dayService.progressForDay(testRequest, date);

        //then
        assertEquals("Твой прогресс за 25.02.2026\uD83D\uDCAA\uD83C\uDFFB:\n", result);
    }

}
