package com.github.sportbot.controller.admin;

import com.github.sportbot.dto.admin.AchievementAdminDto;
import com.github.sportbot.dto.admin.AchievementLocalizationDto;
import com.github.sportbot.dto.admin.AchievementUpdateRequest;
import com.github.sportbot.service.AchievementAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin API for managing achievements and their localizations
 */
@RestController
@RequestMapping("/admin/achievements")
@RequiredArgsConstructor
@Validated
@Tag(name = "Achievement Admin", description = "Admin API for managing achievements")
public class AchievementAdminController {

    private final AchievementAdminService achievementAdminService;

    @GetMapping
    @Operation(summary = "Get all achievements", description = "Returns all achievements with all localizations")
    public ResponseEntity<List<AchievementAdminDto>> getAllAchievements() {
        List<AchievementAdminDto> achievements = achievementAdminService.getAllAchievements();
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get achievement by ID", description = "Returns single achievement with all localizations")
    public ResponseEntity<AchievementAdminDto> getAchievementById(
            @Parameter(description = "Achievement ID") @PathVariable Long id
    ) {
        return achievementAdminService.getAchievementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/localization")
    @Operation(
            summary = "Create or update localization",
            description = "Creates new or updates existing localization for achievement in specified language"
    )
    public ResponseEntity<AchievementLocalizationDto> updateLocalization(
            @Parameter(description = "Achievement ID") @PathVariable Long id,
            @Parameter(description = "Language code (e.g., 'ru', 'en', 'uk')")
            @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a valid ISO 639-1 code (2 lowercase letters)")
            @RequestParam String language,
            @Parameter(description = "Localized title") @RequestParam String title,
            @Parameter(description = "Localized description") @RequestParam String description
    ) {
        try {
            AchievementLocalizationDto result = achievementAdminService.updateLocalization(
                    id, language, title, description
            );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update achievement metadata",
            description = "Updates achievement metadata (reward, active status, emoji, sort order)"
    )
    public ResponseEntity<AchievementAdminDto> updateAchievementMetadata(
            @Parameter(description = "Achievement ID") @PathVariable Long id,
            @RequestBody AchievementUpdateRequest request
    ) {
        try {
            AchievementAdminDto result = achievementAdminService.updateAchievementMetadata(id, request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/localization/{language}")
    @Operation(
            summary = "Delete localization",
            description = "Deletes localization for achievement in specified language"
    )
    public ResponseEntity<Void> deleteLocalization(
            @Parameter(description = "Achievement ID") @PathVariable Long id,
            @Parameter(description = "Language code")
            @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a valid ISO 639-1 code (2 lowercase letters)")
            @PathVariable String language
    ) {
        try {
            achievementAdminService.deleteLocalization(id, language);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
