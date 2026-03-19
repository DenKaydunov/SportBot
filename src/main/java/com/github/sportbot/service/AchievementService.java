package com.github.sportbot.service;

import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.Achievement;
import com.github.sportbot.model.ReferralMilestone;
import com.github.sportbot.model.StreakMilestone;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.AchievementRepository;
import com.github.sportbot.repository.MilestoneRepository;
import com.github.sportbot.repository.ReferralMilestoneRepository;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
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
    private final ReferralMilestoneRepository referralMilestoneRepository;
    private final MessageSource messageSource;
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

    @Transactional
    public void checkReferralMilestones(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Integer referralCount = userRepository.countByReferrerTelegramId(user.getTelegramId().intValue());

        List<ReferralMilestone> milestones =
            referralMilestoneRepository.findByReferralsRequiredLessThanEqual(referralCount);

        List<Long> achievedIds =
            achievementRepository.findReferralMilestoneIdsByUserId(user.getId());
        Set<Long> achievedSet = new HashSet<>(achievedIds);

        for (ReferralMilestone milestone : milestones) {
            if (!achievedSet.contains(milestone.getId())) {
                Achievement achievement = new Achievement();
                achievement.setUser(user);
                achievement.setReferralMilestone(milestone);
                achievement.setAchievedDate(LocalDate.now());

                achievementRepository.save(achievement);

                user.setBalanceTon(user.getBalanceTon() + milestone.getRewardTon());
                userRepository.save(user);
            }
        }
    }

    public String getUserAchievement(Long telegramId){
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
        Locale locale = getUserLocale(user);

        List<Achievement> achieve = achievementRepository.findByUserOrderByAchievedDate(user.getId());

        if (achieve == null || achieve.isEmpty()){
            return messageSource.getMessage("achievement.none.yet", null, locale);
        }

        StringBuilder result = new StringBuilder(
            messageSource.getMessage("achievement.list.header", null, locale)
        ).append("\n");

        achieve.forEach(a -> {
            if (a.getMilestone() != null) {
                result.append(
                    messageSource.getMessage(
                        "achievement.list.item.streak",
                        new Object[]{
                            entityLocalizationService.getStreakMilestoneTitle(a.getMilestone(), locale),
                            a.getMilestone().getDaysRequired(),
                            a.getAchievedDate()
                        },
                        locale
                    )
                ).append("\n");
            } else if (a.getReferralMilestone() != null) {
                result.append(
                    messageSource.getMessage(
                        "achievement.list.item.referral",
                        new Object[]{
                            entityLocalizationService.getReferralMilestoneTitle(a.getReferralMilestone(), locale),
                            a.getReferralMilestone().getReferralsRequired(),
                            a.getAchievedDate()
                        },
                        locale
                    )
                ).append("\n");
            }
        });

        return result.toString();
    }

    private Locale getUserLocale(User user){
        String lang = user.getLanguage();
        if (!"ru".equals(lang) && !"en".equals(lang) && !"uk".equals(lang)){
            lang = "ru";
        }
        return Locale.forLanguageTag(lang);
    }
}