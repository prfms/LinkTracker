package backend.academy.bot.client.dto;

import backend.academy.bot.validation.ValidTrackingUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class AddLinkRequestDto {
    @NotNull
    @NotBlank(message = "Ссылка не может быть пустой")
    @ValidTrackingUrl
    private String link;
    private List<String> tags;
    private List<String> filters;
}
