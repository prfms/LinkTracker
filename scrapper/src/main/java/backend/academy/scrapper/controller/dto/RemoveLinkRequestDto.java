package backend.academy.scrapper.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RemoveLinkRequestDto {
    @NotNull
    @NotBlank(message = "Ссылка не может быть пустой")
    //@ValidTrackingUrl
    private String link;
}
