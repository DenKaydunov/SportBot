package com.github.sportbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request to update user language")
public record UpdateLanguageRequest(
        @NotBlank(message = "Language cannot be blank")
        @Pattern(regexp = "ru|en|uk", message = "Language must be one of: ru, en, uk")
        @Schema(description = "Language code", example = "en", allowableValues = {"ru", "en", "uk"})
        String language
) {
}
