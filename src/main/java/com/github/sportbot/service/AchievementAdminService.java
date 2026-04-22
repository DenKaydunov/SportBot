package com.github.sportbot.service;

import com.github.sportbot.dto.admin.AchievementAdminDto;
import com.github.sportbot.dto.admin.AchievementLocalizationDto;
import com.github.sportbot.dto.admin.AchievementUpdateRequest;
import com.github.sportbot.model.Achievement;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.repository.AchievementDefinitionRepository;
import com.github.sportbot.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing achievements via Admin API
 */
@Service
@RequiredArgsConstructor
public class AchievementAdminService {

    private final AchievementDefinitionRepository achievementDefinitionRepository;
    private final AchievementRepository achievementRepository;

    /**
     * Get all achievements with all localizations
     */
    @Transactional(readOnly = true)
    public List<AchievementAdminDto> getAllAchievements() {
        List<AchievementDefinition> definitions = achievementDefinitionRepository.findAll();

        return definitions.stream()
                .map(this::toAdminDto)
                .collect(Collectors.toList());
    }

    /**
     * Get single achievement by ID with all localizations
     */
    @Transactional(readOnly = true)
    public Optional<AchievementAdminDto> getAchievementById(Long id) {
        return achievementDefinitionRepository.findById(id)
                .map(this::toAdminDto);
    }

    /**
     * Create or update localization for an achievement
     */
    @Transactional
    public AchievementLocalizationDto updateLocalization(
            Long achievementId,
            String language,
            String title,
            String description
    ) {
        AchievementDefinition definition = achievementDefinitionRepository.findById(achievementId)
                .orElseThrow(() -> new IllegalArgumentException("Achievement not found: " + achievementId));

        Achievement achievement = achievementRepository
                .findByAchievementDefinitionAndLanguage(definition, language)
                .orElseGet(() -> {
                    Achievement newAch = new Achievement();
                    newAch.setAchievementDefinition(definition);
                    newAch.setLanguage(language);
                    return newAch;
                });

        achievement.setTitle(title);
        achievement.setDescription(description);

        Achievement saved = achievementRepository.save(achievement);

        return toLocalizationDto(saved);
    }

    /**
     * Update achievement metadata (reward, active status, etc.)
     */
    @Transactional
    public AchievementAdminDto updateAchievementMetadata(
            Long achievementId,
            AchievementUpdateRequest request
    ) {
        AchievementDefinition definition = achievementDefinitionRepository.findById(achievementId)
                .orElseThrow(() -> new IllegalArgumentException("Achievement not found: " + achievementId));

        if (request.getRewardTon() != null) {
            definition.setRewardTon(request.getRewardTon());
        }
        if (request.getIsActive() != null) {
            definition.setIsActive(request.getIsActive());
        }
        if (request.getEmoji() != null) {
            definition.setEmoji(request.getEmoji());
        }
        if (request.getSortOrder() != null) {
            definition.setSortOrder(request.getSortOrder());
        }

        AchievementDefinition saved = achievementDefinitionRepository.save(definition);
        return toAdminDto(saved);
    }

    /**
     * Delete localization for specific language
     */
    @Transactional
    public void deleteLocalization(Long achievementId, String language) {
        AchievementDefinition definition = achievementDefinitionRepository.findById(achievementId)
                .orElseThrow(() -> new IllegalArgumentException("Achievement not found: " + achievementId));

        achievementRepository.findByAchievementDefinitionAndLanguage(definition, language)
                .ifPresent(achievementRepository::delete);
    }

    /**
     * Convert AchievementDefinition to AchievementAdminDto with all localizations
     */
    private AchievementAdminDto toAdminDto(AchievementDefinition definition) {
        List<Achievement> achievements = achievementRepository.findByAchievementDefinition(definition);

        Map<String, AchievementLocalizationDto> localizations = achievements.stream()
                .collect(Collectors.toMap(
                        Achievement::getLanguage,
                        this::toLocalizationDto
                ));

        return AchievementAdminDto.builder()
                .id(definition.getId())
                .code(definition.getCode())
                .category(definition.getCategory())
                .emoji(definition.getEmoji())
                .targetValue(definition.getTargetValue())
                .exerciseTypeId(definition.getExerciseType() != null ? definition.getExerciseType().getId() : null)
                .rewardTon(definition.getRewardTon())
                .sortOrder(definition.getSortOrder())
                .isLegendary(definition.getIsLegendary())
                .isActive(definition.getIsActive())
                .localizations(localizations)
                .build();
    }

    /**
     * Convert Achievement to AchievementLocalizationDto
     */
    private AchievementLocalizationDto toLocalizationDto(Achievement achievement) {
        return AchievementLocalizationDto.builder()
                .language(achievement.getLanguage())
                .title(achievement.getTitle())
                .description(achievement.getDescription())
                .updatedAt(achievement.getUpdatedAt())
                .build();
    }
}
