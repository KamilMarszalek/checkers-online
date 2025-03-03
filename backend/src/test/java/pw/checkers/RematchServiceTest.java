package pw.checkers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import pw.checkers.data.enums.Color;
import pw.checkers.message.GameIdMessage;
import pw.checkers.message.Message;
import pw.checkers.message.PromptMessage;
import pw.checkers.sockets.GameManager;
import pw.checkers.sockets.MessageSender;
import pw.checkers.sockets.RematchService;
import pw.checkers.sockets.SessionManager;

@ExtendWith(MockitoExtension.class)
public class RematchServiceTest {

    @Mock
    private SessionManager sessionManager;
    @Mock
    private MessageSender messageSender;
    @Mock
    private GameManager gameManager;
    @Mock
    private WebSocketSession session1;
    @Mock
    private WebSocketSession session2;

    private RematchService rematchService;

    @BeforeEach
    public void setUp() {
        rematchService = new RematchService(sessionManager, messageSender, gameManager);
        // Mark these stubbing as lenient if they are not used in every test.
        lenient().when(session1.getId()).thenReturn("session1");
        lenient().when(session2.getId()).thenReturn("session2");
    }

    @Test
    public void testProposeRematch_NoSessions() throws IOException {
        String gameId = "game123";
        GameIdMessage msg = new GameIdMessage(gameId);
        // Simulate no sessions available in the game.
        when(sessionManager.getSessionsByGameId(gameId)).thenReturn(null);

        rematchService.proposeRematch(session1, msg);

        // Expect a rejection message sent to session1.
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageSender).sendMessage(eq(session1), captor.capture());
        Message sentMessage = captor.getValue();
        assertEquals("rejection", sentMessage.getType());
        assertTrue(((PromptMessage) sentMessage).getMessage()
                .contains("Opponent has already left the game"));
    }

    @Test
    public void testProposeRematch_NotCompleteAndOpponentPresent() throws IOException {
        String gameId = "game123";
        GameIdMessage msg = new GameIdMessage(gameId);
        // Simulate a game with two sessions.
        Set<WebSocketSession> sessions = new HashSet<>();
        sessions.add(session1);
        sessions.add(session2);
        when(sessionManager.getSessionsByGameId(gameId)).thenReturn(sessions);
        // For session1, opponent is session2.
        when(sessionManager.getOpponent(gameId, session1)).thenReturn(Optional.of(session2));

        rematchService.proposeRematch(session1, msg);

        // In this case, a rematch request message is sent to session2.
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageSender).sendMessage(eq(session2), captor.capture());
        Message sentMessage = captor.getValue();
        assertEquals("rematch request", sentMessage.getType());
    }

    @Test
    public void testProposeRematch_Complete() throws IOException {
        String gameId = "game123";
        GameIdMessage msg = new GameIdMessage(gameId);
        // Simulate a game with two sessions.
        Set<WebSocketSession> sessions = new HashSet<>();
        sessions.add(session1);
        sessions.add(session2);
        when(sessionManager.getSessionsByGameId(gameId)).thenReturn(sessions);
        when(sessionManager.getOpponent(gameId, session1)).thenReturn(Optional.of(session2));
        when(sessionManager.getOpponent(gameId, session2)).thenReturn(Optional.of(session1));

        // First call: session1 proposes rematch.
        rematchService.proposeRematch(session1, msg);
        // Second call: session2 proposes rematch; rematch should now be complete.
        rematchService.proposeRematch(session2, msg);

        // Verify that startRematch is triggered by checking that gameManager.cleanGameHistory and gameManager.createGame was called.
        verify(gameManager, times(1)).cleanGameHistory(msg);
        verify(gameManager, times(1)).createGame();

    }

    @Test
    public void testRemoveFromRematchRequests() {
        // call the removal method and ensure no exception is thrown.
        assertDoesNotThrow(() -> rematchService.removeFromRematchRequests("game123"));
    }

    @Test
    public void testStartRematch_GamePlayersNull() throws IOException {
        String gameId = "game123";
        GameIdMessage msg = new GameIdMessage(gameId);
        // Simulate that no color assignments exist for this game.
        when(sessionManager.getColorAssignments(gameId)).thenReturn(null);

        rematchService.startRematch(session1, msg);

        // Expect that a rejection message is sent to session1.
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageSender).sendMessage(eq(session1), captor.capture());
        Message sentMessage = captor.getValue();
        assertEquals("rejection", sentMessage.getType());
        assertTrue(((PromptMessage) sentMessage).getMessage()
                .contains("Opponent has already left the game"));
    }

    @Test
    public void testStartRematch_ValidScenario() throws IOException {
        String gameId = "game123";
        GameIdMessage msg = new GameIdMessage(gameId);
        // Prepare a fake color assignment map.
        Map<WebSocketSession, String> colorAssignments = new HashMap<>();
        colorAssignments.put(session1, Color.WHITE.getValue());
        colorAssignments.put(session2, Color.BLACK.getValue());
        when(sessionManager.getColorAssignments(gameId)).thenReturn(colorAssignments);

        // Prepare a session-by-color map.
        Map<String, WebSocketSession> playersByColor = Map.of(
                Color.WHITE.getValue(), session1,
                Color.BLACK.getValue(), session2
        );
        when(sessionManager.getSessionByColorMap(gameId)).thenReturn(playersByColor);
        // Stub gameManager.createGame() to return a new game id.
        when(gameManager.createGame()).thenReturn("newGameId");

        rematchService.startRematch(session1, msg);

        // Verify that gameManager.cleanGameHistory was called.
        verify(gameManager, times(1)).cleanGameHistory(msg);
        // Verify that sessionManager.addToSessionsByGame and addToColorAssignments were called with expected arguments.
        verify(sessionManager, times(1)).addToSessionsByGame(eq("newGameId"), eq(session1), eq(session2));
        verify(sessionManager, times(1)).addToColorAssignments(eq("newGameId"), eq(session2), eq(session1));

        // Verify that messageSender.sendMessage is called for both sessions with GAME_CREATED messages.
        ArgumentCaptor<Message> captor1 = ArgumentCaptor.forClass(Message.class);
        verify(messageSender).sendMessage(eq(session1), eq(Color.BLACK.getValue()), captor1.capture());
        Message msg1 = captor1.getValue();
        assertEquals("game created", msg1.getType().toLowerCase());

        ArgumentCaptor<Message> captor2 = ArgumentCaptor.forClass(Message.class);
        verify(messageSender).sendMessage(eq(session2), eq(Color.WHITE.getValue()), captor2.capture());
        Message msg2 = captor2.getValue();
        assertEquals("game created", msg2.getType().toLowerCase());
    }
}
