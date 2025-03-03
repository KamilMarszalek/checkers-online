package pw.checkers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.message.PossibilitiesInput;
import pw.checkers.message.PossibilitiesOutput;
import pw.checkers.message.PromptMessage;
import pw.checkers.message.GameIdMessage;
import pw.checkers.message.Message;
import pw.checkers.message.User;
import pw.checkers.sockets.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CheckersWebSocketHandlerTest {

    @Mock
    private SessionManager sessionManager;
    @Mock
    private RematchService rematchService;
    @Mock
    private MessageSender messageSender;
    @Mock
    private GameManager gameManager;
    @Mock
    private WebSocketSession session;

    private TestableCheckersWebSocketHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new TestableCheckersWebSocketHandler(sessionManager, rematchService, messageSender, gameManager);
        // Lenient stubbing for session ID to avoid unnecessary stubbing warnings.
        lenient().when(session.getId()).thenReturn("session1");
    }

    @Test
    public void testHandleJoinQueue_NoWaitingPlayer() throws Exception {
        // Simulate a joinQueue message with a fake user.
        String jsonPayload = "{\"type\":\"joinQueue\",\"user\":{\"username\":\"Alice\"}}";
        // Stub pollFromPlayerQueue to return null.
        when(sessionManager.pollFromPlayerQueue()).thenReturn(null);

        handler.handleTextMessage(session, new TextMessage(jsonPayload));

        // Verify that addPlayerToQueue is called.
        verify(sessionManager, times(1)).addPlayerToQueue(eq(session), any(User.class));

        // Verify that a waiting message (type "waiting") is sent to session.
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageSender, times(1)).sendMessage(eq(session), captor.capture());
        Message sentMessage = captor.getValue();
        assertEquals("waiting", sentMessage.getType().toLowerCase());
        assertTrue(((PromptMessage) sentMessage).getMessage().contains("Waiting for an opponent"));
    }

    @Test
    public void testHandleMove_NoAssignedColor() throws Exception {
        // Simulate a move message with a given gameId.
        String gameId = "game123";
        String jsonPayload = "{\"type\":\"move\",\"gameId\":\"" + gameId + "\",\"move\":{\"fromRow\":5,\"fromCol\":2,\"toRow\":4,\"toCol\":3}}";        // Stub getAssignedColor to return empty.
        lenient().when(sessionManager.getAssignedColor(eq(gameId), eq(session))).thenReturn(Optional.empty());

        handler.handleTextMessage(session, new TextMessage(jsonPayload));

        // When assigned color is empty, nothing further should happen.
        // For example, verify that messageSender.sendMessage is never called with a possibility message.
        verify(messageSender, never()).sendMessage(eq(session), any(String.class), any(Message.class));
    }

    @Test
    public void testHandlePossibilities_Valid() throws Exception {
        // Simulate a possibilities message.
        String gameId = "game123";
        String jsonPayload = "{\"type\":\"possibilities\",\"gameId\":\"" + gameId + "\",\"row\":2,\"col\":3}";
        // Stub getAssignedColor to return "white".
        when(sessionManager.getAssignedColor(eq(gameId), eq(session))).thenReturn(Optional.of("white"));
        // Stub gameManager.getPossibleMoves to return a fake PossibleMoves.
        PossibilitiesOutput dummyMoves = new PossibilitiesOutput();
        dummyMoves.setMoves(new java.util.ArrayList<>());
        when(gameManager.getPossibleMoves(any(PossibilitiesInput.class), eq(session))).thenReturn(dummyMoves);

        handler.handleTextMessage(session, new TextMessage(jsonPayload));

        // Verify that sendMessage is called with a possibility message.
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageSender, times(1)).sendMessage(eq(session), eq("white"), captor.capture());
        Message sentMessage = captor.getValue();
        assertEquals("possibilities", sentMessage.getType().toLowerCase());
    }

    @Test
    public void testHandleLeaveQueue() throws Exception {
        // Simulate a leaveQueue message.
        String jsonPayload = "{\"type\":\"leaveQueue\",\"user\":{\"username\":\"Alice\"}}";
        handler.handleTextMessage(session, new TextMessage(jsonPayload));
        // Verify that removeWaitingPlayerFromQueue is called.
        verify(sessionManager, times(1)).removeWaitingPlayerFromQueue(eq(session), any(User.class));
    }

    @Test
    public void testHandleRematchDecline() throws Exception {
        // Simulate a decline rematch message.
        String gameId = "game123";
        String jsonPayload = "{\"type\":\"decline rematch\",\"gameId\":\"" + gameId + "\"}";
        handler.handleTextMessage(session, new TextMessage(jsonPayload));
        // Verify that gameManager.cleanGameHistory is called.
        verify(gameManager, times(1)).cleanGameHistory(any(GameIdMessage.class));
        // Verify that removeFromRematchRequests is called.
        verify(rematchService, times(1)).removeFromRematchRequests(eq(gameId));
        // Verify that removeUsersBySessionEntry is called.
        verify(sessionManager, times(1)).removeUsersBySessionEntry(eq(session));
    }

    @Test
    public void testHandleUnknownMessageType() throws Exception {
        // Simulate an unknown message type.
        String jsonPayload = "{\"type\":\"unknownType\"}";
        handler.handleTextMessage(session, new TextMessage(jsonPayload));

        // Verify that messageSender.sendMessage is called with an error message.
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageSender, times(1)).sendMessage(eq(session), captor.capture());
        Message sentMessage = captor.getValue();
        assertEquals("error", sentMessage.getType().toLowerCase());
        assertEquals("Unknown message type", ((PromptMessage) sentMessage).getMessage());
    }
}
