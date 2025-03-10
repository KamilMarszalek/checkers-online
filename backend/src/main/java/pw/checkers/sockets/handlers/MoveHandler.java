package pw.checkers.sockets.handlers;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.data.GameState;
import pw.checkers.message.MoveInputMessage;
import pw.checkers.message.MoveOutputMessage;
import pw.checkers.message.PossibilitiesInputMessage;
import pw.checkers.message.PossibilitiesOutputMessage;
import pw.checkers.sockets.services.GameManager;
import pw.checkers.sockets.services.MessageSender;
import pw.checkers.sockets.services.SessionManager;

import java.io.IOException;
import java.util.Optional;

@Service
public class MoveHandler {
    private final SessionManager sessionManager;
    private final GameManager gameManager;
    private final MessageSender messageSender;

    public MoveHandler(SessionManager sessionManager, GameManager gameManager, MessageSender messageSender) {
        this.sessionManager = sessionManager;
        this.gameManager = gameManager;
        this.messageSender = messageSender;
    }

    public void handleMove(WebSocketSession session, MoveInputMessage moveInputMessage) throws IOException {
        String gameId = moveInputMessage.getGameId();
        Optional<String> maybeColor = sessionManager.getAssignedColor(gameId, session);
        if (maybeColor.isEmpty()) return;
        String assignedColor = maybeColor.get();
        MoveOutputMessage moveOutputMessage = gameManager.makeMove(gameId, moveInputMessage.getMove(), assignedColor);
        GameState updatedState = gameManager.getGame(gameId);

        messageSender.broadcastToGame(sessionManager.getSessionsByGameId(gameId), moveOutputMessage, sessionManager.getColorAssignments(gameId));

        if (updatedState.isFinished()) {
            handleGameEnd(gameId, updatedState);
        }

        if (moveOutputMessage != null && moveOutputMessage.isHasMoreTakes()) {
            handleMoreTakes(gameId, moveOutputMessage, session, assignedColor);
        }
    }

    private void handleGameEnd(String gameId, GameState gameState) throws IOException {
        gameManager.setGameEndReason(gameId, false);
        messageSender.broadcastGameEnd(sessionManager.getSessionsByGameId(gameId),
                gameState, sessionManager.getColorAssignments(gameId));
    }

    private void handleMoreTakes(String gameId, MoveOutputMessage moveOutputMessage, WebSocketSession session, String assignedColor) throws IOException {
        PossibilitiesOutputMessage moves = gameManager.getPossibleMoves(
                new PossibilitiesInputMessage(gameId,
                        moveOutputMessage.getMove().getToRow(),
                        moveOutputMessage.getMove().getToCol()),
                session
        );
        messageSender.sendMessage(session, assignedColor, moves);
    }
}
