package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExercisePeriodRepository;
import com.github.sportbot.repository.ExercisePeriodProjection;
import com.github.sportbot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.DateFormatter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExercisePeriodServiceTest {

    @Mock
    private ExercisePeriodRepository dayRepository;

    @InjectMocks
    private ExercisePeriodService dayService;

    private Long telegramId;
    private String oneDate;
    private String twoDate;


    @BeforeEach
    void setUp(){
        oneDate = "24.02.2026";
        twoDate = "26.02.2026";
        telegramId = 123456L;

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

        //when
        String result = dayService.progressForPeriod(telegramId, oneDate, twoDate);

        //then
        assertEquals("Твой прогресс c 24.02.2026 по 26.02.2026:\nОтжимания - 10\nПодтягивания - 20\n", result);

    }


    @Test
    void getReportForDate_WhenNoExercise_ReturnOnlyHeader(){
        //given
        LocalDate startDate = LocalDate.of(2026, 2, 24);
        LocalDate endDate = LocalDate.of(2026, 2, 26);

        when(dayRepository.getUserProgressByPeriod(telegramId, startDate, endDate)).thenReturn(List.of());

        //when
        String result = dayService.progressForPeriod(telegramId, oneDate, twoDate);

        //then
        assertEquals("Твой прогресс c 24.02.2026 по 26.02.2026:\n", result);
    }

}
