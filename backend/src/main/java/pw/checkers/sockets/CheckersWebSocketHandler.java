package pw.checkers.sockets;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pw.checkers.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.checkers.sockets.handlers.*;

import java.io.IOException;

public class CheckersWebSocketHandler extends TextWebSocketHandler implements MessageVisitor {

    private static final Logger logger = LoggerFactory.getLogger(CheckersWebSocketHandler.class);

    private final SessionManager sessionManager;
    private final JoinQueueHandler joinQueueHandler;
    private final LeaveQueueHandler leaveQueueHandler;
    private final MoveHandler moveHandler;
    private final PossibilitiesHandler possibilitiesHandler;
    private final AcceptRematchHandler acceptRematchHandler;
    private final LeaveHandler leaveHandler;
    private final DeclineRematchHandler declineRematchHandler;
    private final RematchRequestHandler rematchRequestHandler;
    private final ResignHandler resignHandler;
    private final MessageMapper messageMapper;


    public CheckersWebSocketHandler(SessionManager sessionManager, JoinQueueHandler joinQueueHandler, LeaveQueueHandler leaveQueueHandler, MoveHandler moveHandler, PossibilitiesHandler possibilitiesHandler, AcceptRematchHandler acceptRematchHandler, LeaveHandler leaveHandler, DeclineRematchHandler declineRematchHandler, RematchRequestHandler rematchRequestHandler, ResignHandler resignHandler, MessageMapper messageMapper) {
        this.sessionManager = sessionManager;
        this.joinQueueHandler = joinQueueHandler;
        this.leaveQueueHandler = leaveQueueHandler;
        this.moveHandler = moveHandler;
        this.possibilitiesHandler = possibilitiesHandler;
        this.acceptRematchHandler = acceptRematchHandler;
        this.leaveHandler = leaveHandler;
        this.declineRematchHandler = declineRematchHandler;
        this.rematchRequestHandler = rematchRequestHandler;
        this.resignHandler = resignHandler;
        this.messageMapper = messageMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        logger.debug("Connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.debug("Message received: {}", message.getPayload());
        MessageAccept rawMessage = messageMapper.toMessageAccept(session, message);
        rawMessage.accept(this, session);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessionManager.handleSessionClose(session);
        logger.debug("Connection closed: {}", session.getId());
    }

    @Override
    public void visit(JoinQueueMessage message, WebSocketSession session) throws IOException {
        joinQueueHandler.handleJoinQueue(session, message.getUser());
    }

    @Override
    public void visit(LeaveQueueMessage message, WebSocketSession session) {
        leaveQueueHandler.handleLeaveQueue(session, message.getUser());
    }

    @Override
    public void visit(MoveInput message, WebSocketSession session) throws IOException {
        moveHandler.handleMove(session, message);
    }

    @Override
    public void visit(PossibilitiesInput message, WebSocketSession session) throws IOException {
        possibilitiesHandler.handlePossibilities(session, message);
    }

    @Override
    public void visit(AcceptRematchMessage message, WebSocketSession session) throws IOException {
        acceptRematchHandler.handleAcceptRematch(session, message);
    }

    @Override
    public void visit(LeaveMessage message, WebSocketSession session) throws IOException {
        leaveHandler.handleLeave(session, message);
    }

    @Override
    public void visit(DeclineRematchMessage message, WebSocketSession session) throws IOException {
        declineRematchHandler.handleDeclineRematch(session, message);
    }

    @Override
    public void visit(RematchRequestMessage message, WebSocketSession session) throws IOException {
        rematchRequestHandler.handleRematchRequest(session, message);
    }

    @Override
    public void visit(ResignMessage message, WebSocketSession session) throws IOException {
        resignHandler.handleResign(session, message);
    }
}
