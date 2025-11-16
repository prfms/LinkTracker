package backend.academy.scrapper.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RemoveLinkRequestDto(
        @NotNull @NotBlank(message = "Ссылка не может быть пустой")
                // @ValidTrackingUrl
                String link) {}
