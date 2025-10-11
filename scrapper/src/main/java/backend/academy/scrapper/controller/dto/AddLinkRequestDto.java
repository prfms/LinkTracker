package backend.academy.scrapper.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import java.util.List;

public record AddLinkRequestDto (
    @NotNull
    @NotBlank(message = "Ссылка не может быть пустой")
    //@ValidTrackingUrl
    String link,
    List<String> tags,
    List<String> filters
) { }
