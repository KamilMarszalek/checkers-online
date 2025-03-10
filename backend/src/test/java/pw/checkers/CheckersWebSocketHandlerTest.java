package pw.checkers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.message.*;
import pw.checkers.sockets.services.SessionManager;
import pw.checkers.sockets.handlers.*;

@ExtendWith(MockitoExtension.class)
public class CheckersWebSocketHandlerTest {

    @Mock
    private SessionManager sessionManager;
    @Mock
    private JoinQueueHandler joinQueueHandler;
    @Mock
    private LeaveQueueHandler leaveQueueHandler;
    @Mock
    private MoveHandler moveHandler;
    @Mock
    private PossibilitiesHandler possibilitiesHandler;
    @Mock
    private AcceptRematchHandler acceptRematchHandler;
    @Mock
    private LeaveHandler leaveHandler;
    @Mock
    private DeclineRematchHandler declineRematchHandler;
    @Mock
    private RematchRequestHandler rematchRequestHandler;
    @Mock
    private ResignHandler resignHandler;
    @Mock
    private MessageMapper messageMapper;
    @Mock
    private WebSocketSession session;

    private TestableCheckersWebSocketHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new TestableCheckersWebSocketHandler(
                sessionManager,
                joinQueueHandler,
                leaveQueueHandler,
                moveHandler,
                possibilitiesHandler,
                acceptRematchHandler,
                leaveHandler,
                declineRematchHandler,
                rematchRequestHandler,
                resignHandler,
                messageMapper
        );
        // Lenient stubbing for session ID
        lenient().when(session.getId()).thenReturn("session1");
    }

    @Test
    public void testHandleJoinQueue_NoWaitingPlayer() throws Exception {
        // Simulate a joinQueue JSON message.
        String jsonPayload = "{\"type\":\"joinQueue\",\"user\":{\"username\":\"Alice\"}}";
        User user = new User();
        user.setUsername("Alice");

        // Create a JoinQueueMessage corresponding to the payload.
        JoinQueueMessage joinQueueMessage = new JoinQueueMessage();
        joinQueueMessage.setUser(user);
        // Stub the mapper to return our message.
        when(messageMapper.toMessageAccept(eq(session), any(TextMessage.class)))
                .thenReturn(joinQueueMessage);

        handler.handleTextMessage(session, new TextMessage(jsonPayload));

        // Verify that the joinQueueHandler is invoked.
        verify(joinQueueHandler, times(1)).handleJoinQueue(session, user);
    }

    @Test
    public void testHandleMove_NoAssignedColor() throws Exception {
        // Simulate a move JSON message.
        String gameId = "game123";
        String jsonPayload = "{\"type\":\"move\",\"gameId\":\"" + gameId + "\","
                + "\"move\":{\"fromRow\":5,\"fromCol\":2,\"toRow\":4,\"toCol\":3}}";
        // Create a MoveInput message.
        MoveInputMessage moveInputMessage = new MoveInputMessage();
        moveInputMessage.setGameId(gameId);
        // (Assume you have setters or a nested move object for move details.)

        when(messageMapper.toMessageAccept(eq(session), any(TextMessage.class)))
                .thenReturn(moveInputMessage);

        handler.handleTextMessage(session, new TextMessage(jsonPayload));

        // Verify that moveHandler.handleMove is called.
        verify(moveHandler, times(1)).handleMove(session, moveInputMessage);
    }

    @Test
    public void testHandlePossibilities_Valid() throws Exception {
        // Simulate a possibilities JSON message.
        String gameId = "game123";
        String jsonPayload = "{\"type\":\"possibilities\",\"gameId\":\"" + gameId + "\","
                + "\"row\":2,\"col\":3}";
        PossibilitiesInputMessage possibilitiesInputMessage = new PossibilitiesInputMessage();
        possibilitiesInputMessage.setGameId(gameId);
        possibilitiesInputMessage.setRow(2);
        possibilitiesInputMessage.setCol(3);

        when(messageMapper.toMessageAccept(eq(session), any(TextMessage.class)))
                .thenReturn(possibilitiesInputMessage);

        handler.handleTextMessage(session, new TextMessage(jsonPayload));

        // Verify that possibilitiesHandler.handlePossibilities is called.
        verify(possibilitiesHandler, times(1)).handlePossibilities(session, possibilitiesInputMessage);
    }

    @Test
    public void testHandleLeaveQueue() throws Exception {
        // Simulate a leaveQueue JSON message.
        String jsonPayload = "{\"type\":\"leaveQueue\",\"user\":{\"username\":\"Alice\"}}";
        User user = new User();
        user.setUsername("Alice");
        LeaveQueueMessage leaveQueueMessage = new LeaveQueueMessage();
        leaveQueueMessage.setUser(user);

        when(messageMapper.toMessageAccept(eq(session), any(TextMessage.class)))
                .thenReturn(leaveQueueMessage);

        handler.handleTextMessage(session, new TextMessage(jsonPayload));

        // Verify that leaveQueueHandler.handleLeaveQueue is called.
        verify(leaveQueueHandler, times(1)).handleLeaveQueue(session, user);
    }

    @Test
    public void testHandleRematchDecline() throws Exception {
        // Simulate a decline rematch JSON message.
        String gameId = "game123";
        String jsonPayload = "{\"type\":\"declineRematch\",\"gameId\":\"" + gameId + "\"}";
        DeclineRematchMessage declineRematchMessage = new DeclineRematchMessage();
        declineRematchMessage.setGameId(gameId);

        when(messageMapper.toMessageAccept(eq(session), any(TextMessage.class)))
                .thenReturn(declineRematchMessage);

        handler.handleTextMessage(session, new TextMessage(jsonPayload));

        // Verify that declineRematchHandler.handleDeclineRematch is invoked.
        verify(declineRematchHandler, times(1)).handleDeclineRematch(session, declineRematchMessage);
    }

    @Test
    public void testHandleResignMessage() throws Exception {
        // Simulate a resignation JSON message.
        String gameId = "game123";
        String jsonPayload = "{\"type\":\"resign\",\"gameId\":\"" + gameId + "\"}";
        ResignMessage resignMessage = new ResignMessage();
        resignMessage.setGameId(gameId);

        when(messageMapper.toMessageAccept(eq(session), any(TextMessage.class)))
                .thenReturn(resignMessage);

        handler.handleTextMessage(session, new TextMessage(jsonPayload));

        // Verify that resignHandler.handleResign is called.
        verify(resignHandler, times(1)).handleResign(session, resignMessage);
    }

    @Test
    public void testHandleUnknownMessageType() throws Exception {
        // Simulate an unknown JSON message type.
        String jsonPayload = "{\"type\":\"unknownType\"}";
        // Let the mapper throw an exception for an unknown type.
        when(messageMapper.toMessageAccept(eq(session), any(TextMessage.class)))
                .thenThrow(new IllegalArgumentException("Unknown message type"));

        // Verify that an exception is thrown.
        Exception exception = assertThrows(IllegalArgumentException.class, () -> handler.handleTextMessage(session, new TextMessage(jsonPayload)));
        assertEquals("Unknown message type", exception.getMessage());
    }
}
