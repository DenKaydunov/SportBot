package com.github.sportbot.service.achievement;

import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.CompetitorsRepository;
import com.github.sportbot.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialAchievementCheckerTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CompetitorsRepository competitorsRepository;

    @InjectMocks
    private SocialAchievementChecker checker;

    private User user;
    private AchievementDefinition followingDefinition;
    private AchievementDefinition followerDefinition;
    private AchievementDefinition socialHeroDefinition;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);

        followingDefinition = new AchievementDefinition();
        followingDefinition.setCode("SOCIAL_FOLLOWING_10");
        followingDefinition.setTargetValue(10);

        followerDefinition = new AchievementDefinition();
        followerDefinition.setCode("SOCIAL_FOLLOWER_10");
        followerDefinition.setTargetValue(10);

        socialHeroDefinition = new AchievementDefinition();
        socialHeroDefinition.setCode("SOCIAL_HERO");
        socialHeroDefinition.setTargetValue(1);
    }

    @Test
    void testGetCategory() {
        assertEquals(AchievementCategory.SOCIAL, checker.getCategory());
    }

    @Test
    void testCalculateProgress_Following_Success() {
        when(subscriptionRepository.countFollowingByUser(user)).thenReturn(15L);

        int progress = checker.calculateProgress(user, followingDefinition);

        assertEquals(15, progress);
        verify(subscriptionRepository, times(1)).countFollowingByUser(user);
    }

    @Test
    void testCalculateProgress_Following_NoFollowing() {
        when(subscriptionRepository.countFollowingByUser(user)).thenReturn(0L);

        int progress = checker.calculateProgress(user, followingDefinition);

        assertEquals(0, progress);
        verify(subscriptionRepository, times(1)).countFollowingByUser(user);
    }

    @Test
    void testCalculateProgress_Following_NullResult() {
        when(subscriptionRepository.countFollowingByUser(user)).thenReturn(null);

        int progress = checker.calculateProgress(user, followingDefinition);

        assertEquals(0, progress);
        verify(subscriptionRepository, times(1)).countFollowingByUser(user);
    }

    @Test
    void testCalculateProgress_Follower_Success() {
        when(subscriptionRepository.countFollowersByUser(user)).thenReturn(20L);

        int progress = checker.calculateProgress(user, followerDefinition);

        assertEquals(20, progress);
        verify(subscriptionRepository, times(1)).countFollowersByUser(user);
    }

    @Test
    void testCalculateProgress_Follower_NoFollowers() {
        when(subscriptionRepository.countFollowersByUser(user)).thenReturn(0L);

        int progress = checker.calculateProgress(user, followerDefinition);

        assertEquals(0, progress);
        verify(subscriptionRepository, times(1)).countFollowersByUser(user);
    }

    @Test
    void testCalculateProgress_Follower_NullResult() {
        when(subscriptionRepository.countFollowersByUser(user)).thenReturn(null);

        int progress = checker.calculateProgress(user, followerDefinition);

        assertEquals(0, progress);
        verify(subscriptionRepository, times(1)).countFollowersByUser(user);
    }

    @Test
    void testCalculateProgress_SocialHero_Success() {
        // User has 10+ followers and is in top-10 in at least one exercise
        when(subscriptionRepository.countFollowersByUser(user)).thenReturn(15L);
        when(competitorsRepository.isUserInTopNAnyExercise(user.getId(), 10)).thenReturn(true);

        int progress = checker.calculateProgress(user, socialHeroDefinition);

        assertEquals(1, progress);
        verify(subscriptionRepository, times(1)).countFollowersByUser(user);
        verify(competitorsRepository, times(1)).isUserInTopNAnyExercise(user.getId(), 10);
    }

    @Test
    void testCalculateProgress_SocialHero_NotEnoughFollowers() {
        // User has less than 10 followers
        when(subscriptionRepository.countFollowersByUser(user)).thenReturn(5L);

        int progress = checker.calculateProgress(user, socialHeroDefinition);

        assertEquals(0, progress);
        verify(subscriptionRepository, times(1)).countFollowersByUser(user);
        verify(competitorsRepository, never()).isUserInTopNAnyExercise(anyInt(), anyInt());
    }

    @Test
    void testCalculateProgress_SocialHero_NotInTop10() {
        // User has 10+ followers but is not in top-10 in any exercise
        when(subscriptionRepository.countFollowersByUser(user)).thenReturn(15L);
        when(competitorsRepository.isUserInTopNAnyExercise(user.getId(), 10)).thenReturn(false);

        int progress = checker.calculateProgress(user, socialHeroDefinition);

        assertEquals(0, progress);
        verify(subscriptionRepository, times(1)).countFollowersByUser(user);
        verify(competitorsRepository, times(1)).isUserInTopNAnyExercise(user.getId(), 10);
    }

    @Test
    void testCalculateProgress_SocialHero_NullFollowerCount() {
        // User has null followers
        when(subscriptionRepository.countFollowersByUser(user)).thenReturn(null);

        int progress = checker.calculateProgress(user, socialHeroDefinition);

        assertEquals(0, progress);
        verify(subscriptionRepository, times(1)).countFollowersByUser(user);
        verify(competitorsRepository, never()).isUserInTopNAnyExercise(anyInt(), anyInt());
    }

    @Test
    void testCalculateProgress_SocialHero_NullTop10Result() {
        // User has 10+ followers but repository returns null for top-10 check
        when(subscriptionRepository.countFollowersByUser(user)).thenReturn(15L);
        when(competitorsRepository.isUserInTopNAnyExercise(user.getId(), 10)).thenReturn(null);

        int progress = checker.calculateProgress(user, socialHeroDefinition);

        assertEquals(0, progress);
        verify(subscriptionRepository, times(1)).countFollowersByUser(user);
        verify(competitorsRepository, times(1)).isUserInTopNAnyExercise(user.getId(), 10);
    }

    @Test
    void testCalculateProgress_NullUser() {
        int progress = checker.calculateProgress(null, followingDefinition);

        assertEquals(0, progress);
        verify(subscriptionRepository, never()).countFollowingByUser(any());
    }

    @Test
    void testCalculateProgress_NullDefinitionCode() {
        AchievementDefinition nullCodeDefinition = new AchievementDefinition();
        nullCodeDefinition.setCode(null);

        int progress = checker.calculateProgress(user, nullCodeDefinition);

        assertEquals(0, progress);
        verify(subscriptionRepository, never()).countFollowingByUser(any());
        verify(subscriptionRepository, never()).countFollowersByUser(any());
    }

    @Test
    void testCalculateProgress_UnknownAchievementCode() {
        AchievementDefinition unknownDefinition = new AchievementDefinition();
        unknownDefinition.setCode("UNKNOWN_ACHIEVEMENT");

        int progress = checker.calculateProgress(user, unknownDefinition);

        assertEquals(0, progress);
        verify(subscriptionRepository, never()).countFollowingByUser(any());
        verify(subscriptionRepository, never()).countFollowersByUser(any());
    }

    @Test
    void testCalculateProgress_SingleQuery_AvoidN1Problem() {
        // This test verifies that SOCIAL_HERO uses only ONE query for top-10 check
        // regardless of number of exercise types (4 in production)
        when(subscriptionRepository.countFollowersByUser(user)).thenReturn(15L);
        when(competitorsRepository.isUserInTopNAnyExercise(user.getId(), 10)).thenReturn(true);

        checker.calculateProgress(user, socialHeroDefinition);

        // Verify only ONE repository call was made for top-10 check (not 4)
        verify(competitorsRepository, times(1)).isUserInTopNAnyExercise(user.getId(), 10);
    }
}
