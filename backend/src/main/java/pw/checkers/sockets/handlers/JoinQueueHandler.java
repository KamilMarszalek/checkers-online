package pw.checkers.sockets.handlers;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.data.enums.Color;
import pw.checkers.message.JoinMessage;
import pw.checkers.message.Message;
import pw.checkers.message.PromptMessage;
import pw.checkers.message.User;
import pw.checkers.sockets.GameManager;
import pw.checkers.sockets.MessageSender;
import pw.checkers.sockets.SessionManager;
import pw.checkers.utils.WaitingPlayer;

import java.io.IOException;

import static pw.checkers.data.enums.MessageType.WAITING;
import static pw.checkers.utils.Constants.WAITING_MESSAGE;

@Service
public class JoinQueueHandler {
    private final SessionManager sessionManager;
    private final MessageSender messageSender;
    private final GameManager gameManager;

    public JoinQueueHandler(SessionManager sessionManager, MessageSender messageSender, GameManager gameManager) {
        this.sessionManager = sessionManager;
        this.messageSender = messageSender;
        this.gameManager = gameManager;
    }

    public void handleJoinQueue(WebSocketSession session, User user) throws IOException {
        WaitingPlayer waitingPlayer = sessionManager.pollFromPlayerQueue();

        if (waitingPlayer == null) {
            addPlayerToQueue(session, user);
        } else {
            createAndAssignGame(waitingPlayer, session, user);
        }
    }

    private void addPlayerToQueue(WebSocketSession session, User user) throws IOException {
        sessionManager.addPlayerToQueue(session, user);
        Message waitingMessage = new PromptMessage(WAITING.getValue(), WAITING_MESSAGE);
        messageSender.sendMessage(session, waitingMessage);
    }

    private void createAndAssignGame(WaitingPlayer waitingPlayer, WebSocketSession session, User user) throws IOException {
        WebSocketSession waitingSession = waitingPlayer.session();
        String newGameId = gameManager.createGame();

        updateSessionManager(waitingPlayer, session, user, newGameId);

        Message waitingPlayerResponse = new JoinMessage(newGameId, Color.WHITE.getValue(), new User(user.getUsername()));
        Message sessionPlayerResponse = new JoinMessage(newGameId, Color.BLACK.getValue(), new User(waitingPlayer.user().getUsername()));

        messageSender.sendMessage(waitingSession, Color.WHITE.getValue(), waitingPlayerResponse);
        messageSender.sendMessage(session, Color.BLACK.getValue(), sessionPlayerResponse);
    }

    private void updateSessionManager(WaitingPlayer waitingPlayer, WebSocketSession session, User user, String newGameId) {
        WebSocketSession waitingSession = waitingPlayer.session();
        sessionManager.addToSessionsByGame(newGameId, waitingSession, session);
        sessionManager.addToColorAssignments(newGameId, waitingSession, session);
        sessionManager.addToUserBySessions(waitingSession, waitingPlayer.user(), session, user);
    }
}
