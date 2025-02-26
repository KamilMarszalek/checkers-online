package pw.checkers.sockets;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.data.enums.Color;
import pw.checkers.message.GameIdMessage;
import pw.checkers.message.JoinMessage;
import pw.checkers.message.Message;
import pw.checkers.message.PromptMessage;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static pw.checkers.data.enums.MessageType.*;
import static pw.checkers.data.enums.MessageType.GAME_CREATED;

@Service
public class RematchService {
    private final Map<String, Set<WebSocketSession>> rematchRequests = new ConcurrentHashMap<>();
    private final SessionManager sessionManager;
    private final MessageSender messageSender;
    private final GameManager gameManager;

    public RematchService(SessionManager sessionManager, MessageSender messageSender, GameManager gameManager) {
        this.sessionManager = sessionManager;
        this.messageSender = messageSender;
        this.gameManager = gameManager;
    }

    public void proposeRematch(WebSocketSession session, GameIdMessage gameIdMessage) throws IOException {
        String gameId = gameIdMessage.getGameId();
        Set<WebSocketSession> sessions = sessionManager.getSessionsByGameId(gameId);
        if (sessions == null) {
            Message<PromptMessage> message =
                    new Message<>(REJECTION.getValue(), new PromptMessage("Opponent has already left the game"));
            messageSender.sendMessage(session, message);
            return;
        }
        Optional<WebSocketSession> opponent = sessionManager.getOpponent(gameId, session);

        rematchRequests.putIfAbsent(gameId, ConcurrentHashMap.newKeySet());
        Set<WebSocketSession> rematchSet = rematchRequests.get(gameId);

        synchronized (rematchSet) {
            rematchSet.add(session);
            if (rematchSet.size() == sessions.size()) {
                startRematch(session, gameIdMessage);
                rematchRequests.remove(gameId);
                return;
            }
        }

        if (opponent.isPresent()) {
            Message<GameIdMessage> message =
                    new Message<>(REMATCH_REQUEST.getValue(), new GameIdMessage(gameId));
            messageSender.sendMessage(opponent.get(), message);
        } else {
            Message<PromptMessage> message =
                    new Message<>(REJECTION.getValue(), new PromptMessage("Opponent has already left the game"));
            messageSender.sendMessage(session, message);
        }

    }

    private void setUpRematch(String newGameId, Map<String, WebSocketSession> playersByColor) throws IOException {
        sessionManager.addToSessionsByGame(newGameId, playersByColor.get(Color.WHITE.getValue()), playersByColor.get(Color.BLACK.getValue()));
        sessionManager.addToColorAssignments(newGameId, playersByColor.get(Color.BLACK.getValue()), playersByColor.get(Color.WHITE.getValue()));

        // white player will play black in rematch
        Message<JoinMessage> messageForOriginalWhite = new Message<>(
                GAME_CREATED.getValue(),
                new JoinMessage(newGameId, Color.BLACK.getValue(), sessionManager.getUserBySession(playersByColor.get(Color.BLACK.getValue())))
        );
        Message<JoinMessage> messageForOriginalBlack = new Message<>(
                GAME_CREATED.getValue(),
                new JoinMessage(newGameId, Color.WHITE.getValue(), sessionManager.getUserBySession(playersByColor.get(Color.WHITE.getValue())))
        );
        messageSender.sendMessage(playersByColor.get(Color.WHITE.getValue()), Color.BLACK.getValue(), messageForOriginalWhite);
        messageSender.sendMessage(playersByColor.get(Color.BLACK.getValue()), Color.WHITE.getValue(), messageForOriginalBlack);
    }

    public void removeFromRematchRequests(String gameId) {
        rematchRequests.remove(gameId);
    }

    public void startRematch(WebSocketSession session, GameIdMessage gameIdMessage) throws IOException {
        String gameId = gameIdMessage.getGameId();
        Map<WebSocketSession, String> gamePlayers = sessionManager.getColorAssignments(gameId);
        if (gamePlayers == null) {
            Message<PromptMessage> message = new Message<>(REJECTION.getValue(), new PromptMessage("Opponent has already left the game"));
            messageSender.sendMessage(session, message);
            return;
        }
        Map<String, WebSocketSession> playersByColor = sessionManager.getSessionByColorMap(gameId);
        gameManager.cleanGameHistory(gameIdMessage);
        String newGameId = gameManager.createGame();
        setUpRematch(newGameId, playersByColor);
    }
}
