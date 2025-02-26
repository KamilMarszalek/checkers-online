package pw.checkers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import pw.checkers.message.User;
import pw.checkers.sockets.MessageSender;
import pw.checkers.sockets.SessionManager;

@ExtendWith(MockitoExtension.class)
public class SessionManagerTest {

    @Mock
    private MessageSender messageSender;

    @Mock
    private WebSocketSession session1;
    @Mock
    private WebSocketSession session2;

    // A dummy user for testing
    private User user1;
    private User user2;

    private SessionManager sessionManager;

    @BeforeEach
    public void setUp() {
        sessionManager = new SessionManager(messageSender);
        user1 = new User("Alice");
        user2 = new User("Bob");
        // Stub session IDs (mark lenient to avoid unnecessary stubbing warnings)
        lenient().when(session1.getId()).thenReturn("session1");
        lenient().when(session2.getId()).thenReturn("session2");
    }

    @Test
    public void testRemoveWaitingPlayerFromQueue() {
        // Add a waiting player and then remove it.
        sessionManager.addPlayerToQueue(session1, user1);
        // Verify that polling returns the waiting player.
        assertNotNull(sessionManager.pollFromPlayerQueue(), "Waiting player should be available");
        // Now add again and then remove by calling removeWaitingPlayerFromQueue.
        sessionManager.addPlayerToQueue(session1, user1);
        sessionManager.removeWaitingPlayerFromQueue(session1, user1);
        assertNull(sessionManager.pollFromPlayerQueue(), "Waiting queue should be empty after removal");
    }

    @Test
    public void testGetOpponent_WhenOpponentExists() throws IOException {
        String gameId = "game123";
        // Assume sessions are stored in SessionManager's internal map.
        // We simulate this by adding via addToSessionsByGame.
        sessionManager.addToSessionsByGame(gameId, session1, session2);
        // Now, for session1, the opponent should be session2.
        Optional<WebSocketSession> opponent = sessionManager.getOpponent(gameId, session1);
        assertTrue(opponent.isPresent(), "Opponent should be present");
        assertEquals(session2, opponent.get(), "Opponent should be session2");
    }

    @Test
    public void testGetOpponent_WhenNoSessions() throws IOException {
        // When there is no mapping for the game.
        Optional<WebSocketSession> opponent = sessionManager.getOpponent("nonexistent", session1);
        assertFalse(opponent.isPresent(), "Opponent should be empty when no sessions exist");
    }

    @Test
    public void testRemoveGameFromMaps() {
        String gameId = "game123";
        // Add entries in sessions and color assignments.
        sessionManager.addToSessionsByGame(gameId, session1, session2);
        sessionManager.addToColorAssignments(gameId, session1, session2);
        // Remove the game.
        sessionManager.removeGameFromMaps(gameId);
        assertNull(sessionManager.getSessionsByGameId(gameId), "Sessions for game should be removed");
        assertNull(sessionManager.getColorAssignments(gameId), "Color assignments for game should be removed");
    }

    @Test
    public void testAddToSessionsByGame() {
        String gameId = "game123";
        sessionManager.addToSessionsByGame(gameId, session1, session2);
        Set<WebSocketSession> sessions = sessionManager.getSessionsByGameId(gameId);
        assertNotNull(sessions);
        assertTrue(sessions.contains(session1));
        assertTrue(sessions.contains(session2));
    }

    @Test
    public void testAddToColorAssignments() {
        String gameId = "game123";
        sessionManager.addToColorAssignments(gameId, session1, session2);
        Map<WebSocketSession, String> assignments = sessionManager.getColorAssignments(gameId);
        assertNotNull(assignments);
        // According to implementation, session1 gets Color.WHITE and session2 gets Color.BLACK.
        assertEquals("white", assignments.get(session1).toLowerCase());
        assertEquals("black", assignments.get(session2).toLowerCase());
    }

    @Test
    public void testAddToUserBySessions() {
        sessionManager.addToUserBySessions(session1, user1, session2, user2);
        assertEquals(user1, sessionManager.getUserBySession(session1));
        assertEquals(user2, sessionManager.getUserBySession(session2));
    }

    @Test
    public void testPollFromPlayerQueue() {
        // Initially, the queue is empty.
        assertNull(sessionManager.pollFromPlayerQueue(), "Queue should be empty initially");
        // Add a player.
        sessionManager.addPlayerToQueue(session1, user1);
        assertNotNull(sessionManager.pollFromPlayerQueue(), "Queue should return a waiting player");
    }

    @Test
    public void testAddPlayerToQueue() {
        sessionManager.addPlayerToQueue(session1, user1);
        // Poll the waiting player and check that it matches.
        assertNotNull(sessionManager.pollFromPlayerQueue(), "Queue should return a waiting player");
    }

    @Test
    public void testIsGameIdInvalid() {
        // Without adding any sessions, the game id should be invalid.
        assertTrue(sessionManager.isGameIdInvalid("nonexistent"), "Nonexistent game id should be invalid");
        // Add a game and test.
        String gameId = "game123";
        sessionManager.addToSessionsByGame(gameId, session1, session2);
        assertFalse(sessionManager.isGameIdInvalid(gameId), "Game id should be valid if sessions exist");
    }

    @Test
    public void testGetAssignedColorByGameIdAndSession() {
        String gameId = "game123";
        sessionManager.addToColorAssignments(gameId, session1, session2);
        String color1 = sessionManager.getAssignedColorByGameIdAndSession(gameId, session1);
        String color2 = sessionManager.getAssignedColorByGameIdAndSession(gameId, session2);
        assertEquals("white", color1.toLowerCase());
        assertEquals("black", color2.toLowerCase());
    }

    @Test
    public void testGetSessionByColorMap() {
        String gameId = "game123";
        sessionManager.addToColorAssignments(gameId, session1, session2);
        Map<String, WebSocketSession> sessionByColor = sessionManager.getSessionByColorMap(gameId);
        // We expect that key "white" maps to session1 and "black" maps to session2.
        // (Depending on implementation: check values accordingly)
        assertEquals(session1, sessionByColor.get("white"));
        assertEquals(session2, sessionByColor.get("black"));
    }

    @Test
    public void testRemoveUsersBySessionEntry() {
        sessionManager.addToUserBySessions(session1, user1, session2, user2);
        sessionManager.removeUsersBySessionEntry(session1);
        assertNull(sessionManager.getUserBySession(session1), "User entry should be removed for session1");
        assertNotNull(sessionManager.getUserBySession(session2), "User entry for session2 should remain");
    }

    @Test
    public void testHandleSessionClose() {
        // Set up waiting queue with a waiting player.
        sessionManager.addPlayerToQueue(session1, user1);
        // Add session entries in sessionsByGame and colorAssignmentsByGame.
        String gameId = "game123";
        sessionManager.addToSessionsByGame(gameId, session1, session2);
        sessionManager.addToColorAssignments(gameId, session1, session2);
        // Call handleSessionClose for session1.
        sessionManager.handleSessionClose(session1);
        // Verify that the waiting queue no longer contains session1.
        assertNull(sessionManager.pollFromPlayerQueue(), "Waiting queue should not return session1 after close");
        // Verify that sessionsByGame for gameId no longer contains session1.
        Set<WebSocketSession> sessions = sessionManager.getSessionsByGameId(gameId);
        if (sessions != null) {
            assertFalse(sessions.contains(session1));
        }
        // Verify that colorAssignmentsByGame for gameId no longer contains session1.
        Map<WebSocketSession, String> assignments = sessionManager.getColorAssignments(gameId);
        if (assignments != null) {
            assertFalse(assignments.containsKey(session1));
        }
    }

    @Test
    public void testGetAssignedColor_ErrorConditions() throws IOException {
        // Test when gameId is null.
        doNothing().when(messageSender).sendError(eq(session1), anyString());
        Optional<String> resultNullId = sessionManager.getAssignedColor(null, session1);
        assertTrue(resultNullId.isEmpty(), "Expected empty Optional when gameId is null");
        verify(messageSender).sendError(session1, "No game id specified");

        // Test when gameId is invalid.
        String gameId = "nonexistent";
        Optional<String> resultInvalid = sessionManager.getAssignedColor(gameId, session1);
        assertTrue(resultInvalid.isEmpty(), "Expected empty Optional when game id is invalid");
        verify(messageSender).sendError(session1, "Game with id " + gameId + " not found");

        // Test when assigned color is null.
        gameId = "game123";
        sessionManager.addToSessionsByGame(gameId, session1, session2);
        // Do NOT add color assignment for session1.
        sessionManager.addToColorAssignments(gameId, session2, session2); // fake assignment for session2 only
        Optional<String> resultNoColor = sessionManager.getAssignedColor(gameId, session1);
        assertTrue(resultNoColor.isEmpty(), "Expected empty Optional when no color assigned");
        verify(messageSender).sendError(session1, "You do not belong to this game or no color assigned");

        // Test when assigned color exists.
        sessionManager.addToColorAssignments(gameId, session1, session2); // now session1 gets a color.
        Optional<String> resultValid = sessionManager.getAssignedColor(gameId, session1);
        assertTrue(resultValid.isPresent(), "Expected assigned color to be present");
    }
}
