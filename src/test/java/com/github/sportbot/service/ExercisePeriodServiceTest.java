package com.github.sportbot.service;

import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExercisePeriodProjection;
import com.github.sportbot.repository.ExerciseRecordRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExercisePeriodServiceTest {

    @Mock
    private ExerciseRecordRepository exerciseRepository;
    private ExerciseRecordRepository exerciseRecordRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExerciseService exerciseService;

    private Long telegramId;
    private LocalDate oneDate;
    private LocalDate twoDate;

    private User user;


    @BeforeEach
    void setUp(){
        oneDate = LocalDate.parse("2026-02-24");
        twoDate = LocalDate.parse("2026-02-26");
        telegramId = 123456L;

        user = User.builder()
                .id(1)
                .telegramId(123456L)
                .fullName("Existing User")
                .build();
    }

    @Test
    void getReportForDate_returnsFormattedReport(){
        //given
        LocalDate startDate = LocalDate.of(2026, 2, 24);
        LocalDate endDate = LocalDate.of(2026, 2, 26);

        when(dayRepository.getUserProgressByPeriod(telegramId, startDate, endDate))
                .thenReturn(List.of(
                        new ExercisePeriodProjection() {
                            @Override
                            public String getExerciseType() {
                                return "Отжимания";
                            }
                            @Override
                            public Integer getTotalCount() {
                                return 10;
                            }
                        },
                        new ExercisePeriodProjection() {
                            @Override
                            public String getExerciseType() {
                                return "Подтягивания";
                            }

                            @Override
                            public Integer getTotalCount() {
                                return 20;
                            }
                        }
                ));
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(user));

        //when
        String result = exerciseService.progressForPeriod(telegramId, oneDate, twoDate);

        //then
        assertTrue(result.contains("Твой прогресс с 24.02.2026 по 26.02.2026:"));
        assertTrue(result.contains("Отжимания - 10"));
        assertTrue(result.contains("Подтягивания - 20"));

    }


    @Test
    void getReportForDate_WhenNoExercise(){
        //given
        LocalDate startDate = LocalDate.of(2026, 2, 24);
        LocalDate endDate = LocalDate.of(2026, 2, 26);

        when(exerciseRecordRepository.getUserProgressByPeriod(telegramId, startDate, endDate)).thenReturn(List.of());
        when(exerciseRepository.getUserProgressByPeriod(telegramId, startDate, endDate)).thenReturn(List.of());
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(user));


        //when
        String result = exerciseService.progressForPeriod(telegramId, oneDate, twoDate);

        //then
        assertEquals("Твой прогресс с 24.02.2026 по 26.02.2026:\nТренировок за этот период не найдено. 😴", result);
    }

}
