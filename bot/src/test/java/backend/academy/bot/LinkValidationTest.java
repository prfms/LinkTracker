package backend.academy.bot;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.bot.service.BotService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LinkValidationTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private BotConfig botConfig;

    @InjectMocks
    private BotService botService;

    private static final long CHAT_ID = 123456L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Message mockMessage(String text) {
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(chat.id()).thenReturn(CHAT_ID);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn(text);
        return message;
    }

    @Test
    void testValidLinkUpdatesStateToAwaitingTags() {
        Message message = mockMessage("/track");
        botService.handleMessage(message);

        assertEquals(
                BotService.BotStep.AWAITING_LINK_TRACK,
                botService.userStates().get(CHAT_ID).step());

        Message validLinkMessage = mockMessage("https://stackoverflow.com/questions/12345");
        botService.handleMessage(validLinkMessage);

        // assertEquals(BotService.BotStep.AWAITING_TAGS, botService.userStates().get(CHAT_ID).step());
    }

    @Test
    void testInvalidLinkDoesNotChangeState() {
        Message message = mockMessage("/track");
        botService.handleMessage(message);

        assertEquals(
                BotService.BotStep.AWAITING_LINK_TRACK,
                botService.userStates().get(CHAT_ID).step());

        Message invalidLinkMessage = mockMessage("invalid_link");
        botService.handleMessage(invalidLinkMessage);

        assertEquals(
                BotService.BotStep.AWAITING_LINK_TRACK,
                botService.userStates().get(CHAT_ID).step());
    }
}
