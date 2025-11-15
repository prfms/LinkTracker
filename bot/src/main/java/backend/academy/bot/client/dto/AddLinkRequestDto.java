package backend.academy.bot.client.dto;

import backend.academy.bot.validation.ValidTrackingUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddLinkRequestDto(
    @NotNull
    @NotBlank(message = "Ссылка не может быть пустой")
    @ValidTrackingUrl String link) {}

