package com.github.sportbot.service;

import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.Achievement;
import com.github.sportbot.model.StreakMilestone;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.AchievementRepository;
import com.github.sportbot.repository.MilestoneRepository;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;
    private final MilestoneRepository milestoneRepository;
    private final MessageSource messageSource;
    private final UserService userService;
    private final EntityLocalizationService entityLocalizationService;

    @Transactional
    public void checkStreakMilestones(Long telegramId){
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);

        int currentStreak = user.getCurrentStreak();

        // Получаем все milestone, которые <= текущему streak
        List<StreakMilestone> streakMilestones = milestoneRepository.findByDaysRequiredLessThanEqual(currentStreak);

        List<Achievement> achievedMilestoneId = achievementRepository.findByUserOrderByAchievedDate(user.getId());

        Set<Long> achievedIds = achievedMilestoneId.stream()
                .map(a -> a.getMilestone().getId())
                .collect(Collectors.toSet());

        for (StreakMilestone milestone : streakMilestones){

        //Проверяем было ли уже получено достижение
        if (!achievedIds.contains(milestone.getId())){
            Achievement achieve = new Achievement();

            achieve.setUser(user);
            achieve.setMilestone(milestone);
            achieve.setAchievedDate(LocalDate.now());

            achievementRepository.save(achieve);

            user.setBalanceTon(user.getBalanceTon() + milestone.getRewardTon());

            userRepository.save(user);
        }
        }
    }

    public String getUserAchievement(Long telegramId){
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
        Locale locale = userService.getUserLocale(user);

        List<Achievement> achieve = achievementRepository.findByUserOrderByAchievedDate(user.getId());

        if (achieve == null || achieve.isEmpty()){
            return messageSource.getMessage("achievement.none.yet", null, locale);
        }

        StringBuilder result = new StringBuilder(
            messageSource.getMessage("achievement.list.header", null, locale)
        ).append("\n");

        achieve.forEach(a -> result.append(
            messageSource.getMessage(
                "achievement.list.item",
                new Object[]{
                    entityLocalizationService.getStreakMilestoneTitle(a.getMilestone(), locale),
                    a.getMilestone().getDaysRequired(),
                    a.getAchievedDate()
                },
                locale
            )
        ).append("\n"));

        return result.toString();
    }
}