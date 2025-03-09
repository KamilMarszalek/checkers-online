package pw.checkers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.data.GameState;
import pw.checkers.data.enums.Color;
import pw.checkers.data.enums.GameEndReason;
import pw.checkers.message.GameEnd;
import pw.checkers.message.Message;
import pw.checkers.message.PromptMessage;
import pw.checkers.sockets.MessageSender;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

class MessageSenderTest {

    private MessageSender messageSender;
    private ObjectMapper objectMapper;

    private WebSocketSession session1;
    private WebSocketSession session2;

    @BeforeEach
    void setUp() {
        messageSender = new MessageSender();
        objectMapper = new ObjectMapper();

        // Create mock sessions
        session1 = mock(WebSocketSession.class);
        session2 = mock(WebSocketSession.class);

        when(session1.getId()).thenReturn("session1");
        when(session2.getId()).thenReturn("session2");
    }

    @Test
    void testSendError() throws IOException {
        String errorText = "Test error message";
        messageSender.sendError(session1, errorText);

        // Capture the TextMessage sent to session1
        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session1).sendMessage(captor.capture());

        String sentJson = captor.getValue().getPayload();
        // Deserialize the JSON to a Message
        Message sentMessage = objectMapper.readValue(sentJson, Message.class);
        assertEquals("error", sentMessage.getType(), "Message type should be error");
        assertEquals(errorText, ((PromptMessage)sentMessage).getMessage(), "Message content should match error text");
    }

    @Test
    void testSendMessageWithoutColor() throws IOException {
        Message message = new PromptMessage("info", "Hello world");
        messageSender.sendMessage(session1, message);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session1).sendMessage(captor.capture());

        String sentJson = captor.getValue().getPayload();
        Message sentMessage = objectMapper.readValue(sentJson, Message.class);
        assertEquals("info", sentMessage.getType());
        assertEquals("Hello world", ((PromptMessage)sentMessage).getMessage());
    }

    @Test
    void testSendMessageWithColor() throws IOException {
        Message message = new PromptMessage("info", "Color message");
        messageSender.sendMessage(session1, "red", message);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session1).sendMessage(captor.capture());

        String sentJson = captor.getValue().getPayload();
        Message sentMessage = objectMapper.readValue(sentJson, Message.class);
        assertEquals("info", sentMessage.getType());
        assertEquals("Color message", ((PromptMessage) sentMessage).getMessage());
        // The color parameter is used only for logging.
    }

    @Test
    void testBroadcastToGame() throws IOException {
        // Create a set of sessions and a color mapping.
        Set<WebSocketSession> sessions = new HashSet<>();
        sessions.add(session1);
        sessions.add(session2);
        when(session1.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(true);

        Map<WebSocketSession, String> colorByPlayer = new HashMap<>();
        colorByPlayer.put(session1, "blue");
        colorByPlayer.put(session2, "green");

        Message message = new PromptMessage("info", "Broadcast test");
        messageSender.broadcastToGame(sessions, message, colorByPlayer);

        // Verify that sendMessage(session, color, message) is called for both sessions.
        ArgumentCaptor<TextMessage> captor1 = ArgumentCaptor.forClass(TextMessage.class);
        verify(session1).sendMessage(captor1.capture());
        String json1 = captor1.getValue().getPayload();
        Message m1 = objectMapper.readValue(json1, Message.class);
        assertEquals("info", m1.getType());
        assertEquals("Broadcast test", ((PromptMessage) m1).getMessage());

        ArgumentCaptor<TextMessage> captor2 = ArgumentCaptor.forClass(TextMessage.class);
        verify(session2).sendMessage(captor2.capture());
        String json2 = captor2.getValue().getPayload();
        Message m2 = objectMapper.readValue(json2, Message.class);
        assertEquals("info", m2.getType());
        assertEquals("Broadcast test", ((PromptMessage) m2).getMessage());
    }

    @Test
    void testBroadcastGameEnd_Draw() throws IOException {
        // Create a set of sessions and a color mapping.
        Set<WebSocketSession> sessions = new HashSet<>();
        sessions.add(session1);
        sessions.add(session2);
        when(session1.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(true);

        // Create a GameState with no winner (draw).
        GameState state = new GameState();
        state.setWinner(null);
        state.setGameEndReason(GameEndReason.FIFTY_MOVES);

        Map<WebSocketSession, String> colorByPlayer = new HashMap<>();
        colorByPlayer.put(session1, "red");
        colorByPlayer.put(session2, "blue");

        messageSender.broadcastGameEnd(sessions, state, colorByPlayer);

        // Verify that both sessions receive a gameEnd message with "draw"
        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session1).sendMessage(captor.capture());
        String json = captor.getValue().getPayload();
        Message received = objectMapper.readValue(json, Message.class);
        assertEquals("gameEnd", received.getType());
        assertEquals("draw", ((GameEnd) received).getResult());
        assertEquals("draw", ((GameEnd) received).getResult());
        assertEquals("fiftyMoves", ((GameEnd) received).getDetails());
    }

    @Test
    void testBroadcastGameEnd_WithWinner() throws IOException {
        // Create a set of sessions and a color mapping.
        Set<WebSocketSession> sessions = new HashSet<>();
        sessions.add(session1);
        sessions.add(session2);
        when(session1.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(true);

        // Create a GameState with a winner.
        GameState state = new GameState();
        state.setWinner(Color.WHITE);
        state.setGameEndReason(GameEndReason.NO_PIECES);

        Map<WebSocketSession, String> colorByPlayer = new HashMap<>();
        colorByPlayer.put(session1, "red");
        colorByPlayer.put(session2, "blue");

        messageSender.broadcastGameEnd(sessions, state, colorByPlayer);

        // Verify that at least one session receives a gameEnd message with the winner "white"
        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session1).sendMessage(captor.capture());
        String json = captor.getValue().getPayload();
        Message received = objectMapper.readValue(json, Message.class);
        assertEquals("gameEnd", received.getType());
        assertTrue(((GameEnd)received).getResult().equalsIgnoreCase("white"));
    }
}
