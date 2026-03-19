package com.github.sportbot.service;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.LeaderBoardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private LeaderBoardRepository leaderBoardRepository;

    @Mock
    private ExerciseTypeService exerciseTypeService;

    @Mock
    private TagService tagService;

    @Mock
    private UserService userService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private EntityLocalizationService entityLocalizationService;

    @InjectMocks
    private LeaderboardService leaderboardService;

    private ExerciseType exerciseType;
    private User testUser;
    private Locale testLocale;

    @BeforeEach
    void setUp() {
        exerciseType = ExerciseType.builder()
                .id(1L)
                .code("push_up")
                .title("Отжимания")
                .build();

        testUser = User.builder()
                .id(1)
                .telegramId(1000001L)
                .language("ru")
                .build();

        testLocale = Locale.forLanguageTag("ru");

        // Setup common mocks with lenient
        lenient().when(userService.getUserLocale(any(User.class))).thenReturn(testLocale);
        lenient().when(entityLocalizationService.getExerciseTypeTitle(any(ExerciseType.class), any(Locale.class)))
                .thenAnswer(inv -> ((ExerciseType) inv.getArgument(0)).getTitle().toLowerCase());
        lenient().when(messageSource.getMessage(eq("leaderboard.header"), any(), any(Locale.class)))
                .thenReturn("⚡Таблица лидеров⚡");
        lenient().when(messageSource.getMessage(eq("leaderboard.period.label"), any(), any(Locale.class)))
                .thenAnswer(invocation -> "Период: " + ((Object[])invocation.getArgument(1))[0]);
        lenient().when(messageSource.getMessage(eq("leaderboard.total.users.made"), any(), any(Locale.class)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArgument(1);
                    return "Всего пользователи сделали: " + args[0] + " " + args[1] + ".";
                });
        lenient().when(messageSource.getMessage(eq("leaderboard.no.records"), any(), any(Locale.class)))
                .thenReturn("Нет записей за выбранный период.");
        lenient().when(messageSource.getMessage(eq("leaderboard.period.from.to"), any(), any(Locale.class)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArgument(1);
                    return "c " + args[0] + " по " + args[1];
                });
    }

    @Test
    void getLeaderboardByPeriod_AllTime_ReturnsFormattedLeaderboard() {
        // Given
        when(exerciseTypeService.getExerciseType("push_up")).thenReturn(exerciseType);
        when(leaderBoardRepository.sumCountByExerciseTypeAndDate(1L, null, null, null))
                .thenReturn(500);
        when(leaderBoardRepository.findTopUsersByExerciseTypeAndDate(1L, null, 10, null, null))
                .thenReturn(Arrays.asList(
                        new Object[]{"User 1", 200L},
                        new Object[]{"User 2", 150L},
                        new Object[]{"User 3", 150L}
                ));

        // When
        String result = leaderboardService.getLeaderboardByPeriod("push_up", 10, "all", testUser);

        // Then
        assertTrue(result.contains("⚡Таблица лидеров⚡"));
        assertTrue(result.contains("За все время"));
        assertTrue(result.contains("500"));
        assertTrue(result.contains("отжимания"));
        assertTrue(result.contains("1. User 1 — 200"));
        assertTrue(result.contains("2. User 2 — 150"));
        assertTrue(result.contains("3. User 3 — 150"));
    }

    @Test
    void getLeaderboardByPeriod_Week_ReturnsFormattedLeaderboard() {
        // Given
        when(exerciseTypeService.getExerciseType("push_up")).thenReturn(exerciseType);
        when(leaderBoardRepository.sumCountByExerciseTypeAndDate(eq(1L), isNull(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(150);
        when(leaderBoardRepository.findTopUsersByExerciseTypeAndDate(eq(1L), isNull(), eq(10), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(
                        new Object[]{"User 1", 80L},
                        new Object[]{"User 2", 70L}
                ));

        // When
        String result = leaderboardService.getLeaderboardByPeriod("push_up", 10, "week", testUser);

        // Then
        assertTrue(result.contains("⚡Таблица лидеров⚡"));
        assertTrue(result.contains("За неделю"));
        assertTrue(result.contains("150"));
        assertTrue(result.contains("1. User 1 — 80"));
        assertTrue(result.contains("2. User 2 — 70"));
    }

    @Test
    void getLeaderboardByPeriod_EmptyResults_ShowsNoRecordsMessage() {
        // Given
        when(exerciseTypeService.getExerciseType("push_up")).thenReturn(exerciseType);
        when(leaderBoardRepository.sumCountByExerciseTypeAndDate(anyLong(), isNull(), any(), any()))
                .thenReturn(0);
        when(leaderBoardRepository.findTopUsersByExerciseTypeAndDate(anyLong(), isNull(), anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());

        // When
        String result = leaderboardService.getLeaderboardByPeriod("push_up", 10, "today", testUser);

        // Then
        assertTrue(result.contains("Нет записей за выбранный период"));
    }

    @Test
    void getLeaderboardByPeriodPaged_ReturnsFormattedLeaderboardWithOffset() {
        // Given
        Pageable pageable = PageRequest.of(1, 5); // Page 1, size 5
        when(exerciseTypeService.getExerciseType("push_up")).thenReturn(exerciseType);
        when(leaderBoardRepository.sumCountByExerciseTypeAndDate(eq(1L), isNull(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(250);
        when(leaderBoardRepository.findTopUsersByExerciseTypeAndDatePaged(eq(1L), isNull(), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .then(invocation -> {
                    java.util.List<Object[]> result = new java.util.ArrayList<>();
                    result.add(new Object[]{"User 6", 50L});
                    result.add(new Object[]{"User 7", 45L});
                    return result;
                });

        // When
        String result = leaderboardService.getLeaderboardByPeriodPaged("push_up", pageable, "week", testUser);

        // Then
        assertTrue(result.contains("⚡Таблица лидеров⚡"));
        assertTrue(result.contains("250"));
        // Index should start from offset + 1 = 6
        assertTrue(result.contains("6. User 6 — 50"));
        assertTrue(result.contains("7. User 7 — 45"));
    }

    @Test
    void getLeaderboardByDates_CustomDateRange_ReturnsFormattedLeaderboard() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        when(exerciseTypeService.getExerciseType("push_up")).thenReturn(exerciseType);
        when(tagService.getIdByCode("tag1")).thenReturn(10L);
        when(leaderBoardRepository.sumCountByExerciseTypeAndDate(1L, 10L, startDate, endDate))
                .thenReturn(300);
        when(leaderBoardRepository.findTopUsersByExerciseTypeAndDate(1L, 10L, 20, startDate, endDate))
                .thenReturn(Arrays.asList(
                        new Object[]{"User A", 120L},
                        new Object[]{"User B", 100L}
                ));

        // When
        String result = leaderboardService.getLeaderboardByDates("push_up", "tag1", 20, startDate, endDate, testUser);

        // Then
        assertTrue(result.contains("⚡Таблица лидеров⚡"));
        assertTrue(result.contains("c 2024-01-01 по 2024-01-31"));
        assertTrue(result.contains("300"));
        assertTrue(result.contains("1. User A — 120"));
        assertTrue(result.contains("2. User B — 100"));
    }

    @Test
    void getLeaderboardByDatesPaged_CustomDateRangeWithPagination_ReturnsFormattedLeaderboard() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        Pageable pageable = PageRequest.of(0, 10);

        when(exerciseTypeService.getExerciseType("push_up")).thenReturn(exerciseType);
        when(tagService.getIdByCode("tag1")).thenReturn(10L);
        when(leaderBoardRepository.sumCountByExerciseTypeAndDate(1L, 10L, startDate, endDate))
                .thenReturn(200);
        when(leaderBoardRepository.findTopUsersByExerciseTypeAndDatePaged(eq(1L), eq(10L), eq(startDate), eq(endDate), any(Pageable.class)))
                .then(invocation -> {
                    java.util.List<Object[]> result = new java.util.ArrayList<>();
                    result.add(new Object[]{"User X", 90L});
                    return result;
                });

        // When
        String result = leaderboardService.getLeaderboardByDatesPaged("push_up", "tag1", pageable, startDate, endDate, testUser);

        // Then
        assertTrue(result.contains("⚡Таблица лидеров⚡"));
        assertTrue(result.contains("c 2024-01-01 по 2024-01-31"));
        assertTrue(result.contains("200"));
        assertTrue(result.contains("1. User X — 90"));
    }

    @Test
    void getLeaderboardByPeriod_Month_UsesCorrectPeriodCalculation() {
        // Given
        when(exerciseTypeService.getExerciseType("push_up")).thenReturn(exerciseType);
        when(leaderBoardRepository.sumCountByExerciseTypeAndDate(anyLong(), isNull(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(100);
        when(leaderBoardRepository.findTopUsersByExerciseTypeAndDate(anyLong(), isNull(), anyInt(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        String result = leaderboardService.getLeaderboardByPeriod("push_up", 5, "month", testUser);

        // Then
        assertTrue(result.contains("За месяц"));
        verify(leaderBoardRepository).findTopUsersByExerciseTypeAndDate(
                eq(1L), isNull(), eq(5), any(LocalDate.class), any(LocalDate.class)
        );
    }
}
