package backend.academy.bot;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.bot.client.dto.LinkUpdateDto;
import backend.academy.bot.controller.BotController;
import backend.academy.bot.service.BotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BotController.class)
@ExtendWith(MockitoExtension.class)
public class BotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BotService botService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void updates_ShouldCallSendUpdate_ForEachUser() throws Exception {
        // Arrange
        LinkUpdateDto update = new LinkUpdateDto(
                0, "https://github.com/example/repo", "Обновление в репозитории", List.of(123L, 456L));

        // Act
        mockMvc.perform(post("/api/bot/updates")
                        .content(mapper.writeValueAsString(update))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Assert
        verify(botService, times(1)).sendUpdate(123L, "https://github.com/example/repo");
        verify(botService, times(1)).sendUpdate(456L, "https://github.com/example/repo");
    }
}
