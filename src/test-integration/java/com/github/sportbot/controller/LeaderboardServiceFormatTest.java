package com.github.sportbot.controller;

import com.github.sportbot.repository.LeaderBoardRepository;
import com.github.sportbot.service.ExerciseTypeService;
import com.github.sportbot.service.LeaderboardService;
import com.github.sportbot.service.TagService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceFormatTest {

    @Mock
    private LeaderBoardRepository leaderBoardRepository;

    @Mock
    private ExerciseTypeService exerciseTypeService;

    @Mock
    private TagService tagService;

    @InjectMocks
    private LeaderboardService leaderboardService;

    @Test
    void shouldFormatTopWithUserOutsideTop() {
        // given
        List<Object[]> dbRows = List.of(
                new Object[]{1L, "Test User 1", 100L, 1},
                new Object[]{2L, "Test User 2", 80L, 2},
                new Object[]{3L, "Test User 3", 60L, 3},
                new Object[]{4L, "You", 5L, 7}
        );

        when(leaderBoardRepository.findTopAllWithUser(3, 4L))
                .thenReturn(dbRows);

        // when
        String result = leaderboardService.getTopAllExercises(4L, 3);

        // then
        assertThat(result).contains("Топ:");
        assertThat(result).contains("🥇 1 место — Test User 1 + 100 упр");
        assertThat(result).contains("🥈 2 место — Test User 2 + 80 упр");
        assertThat(result).contains("🥉 3 место — Test User 3 + 60 упр");
        assertThat(result).contains("Твое место — 7 (ты + 5 упр)");
    }
}
