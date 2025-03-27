package backend.academy.bot;

import backend.academy.bot.service.BotService;
import backend.academy.bot.client.ScrapperClient;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private BotConfig botConfig;

    @InjectMocks
    private BotService botService;

    @Test
    void handleMessage_ShouldRespondWithUnknownCommandMessage_WhenCommandIsUnknown() {
        // Act
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(chat.id()).thenReturn(123L);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn("/unknown");

        // Arrange
        botService.handleMessage(message);

        // Assert
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());

        SendMessage sentMessage = captor.getValue();
        assert sentMessage.getParameters().get("text").equals("Неизвестная команда. Используйте /help для списка команд.");
        assert sentMessage.getParameters().get("chat_id").equals(123L);
    }
}
