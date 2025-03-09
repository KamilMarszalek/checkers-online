package pw.checkers.sockets.handlers;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.data.GameState;
import pw.checkers.message.MoveInput;
import pw.checkers.message.MoveOutput;
import pw.checkers.message.PossibilitiesInput;
import pw.checkers.message.PossibilitiesOutput;
import pw.checkers.sockets.GameManager;
import pw.checkers.sockets.MessageSender;
import pw.checkers.sockets.SessionManager;

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

    public void handleMove(WebSocketSession session, MoveInput moveInput) throws IOException {
        String gameId = moveInput.getGameId();
        Optional<String> maybeColor = sessionManager.getAssignedColor(gameId, session);
        if (maybeColor.isEmpty()) return;
        String assignedColor = maybeColor.get();
        MoveOutput moveOutput = gameManager.makeMove(gameId, moveInput.getMove(), assignedColor);
        GameState updatedState = gameManager.getGame(gameId);

        messageSender.broadcastToGame(sessionManager.getSessionsByGameId(gameId), moveOutput, sessionManager.getColorAssignments(gameId));

        if (updatedState.isFinished()) {
            handleGameEnd(gameId, updatedState);
        }

        if (moveOutput != null && moveOutput.isHasMoreTakes()) {
            handleMoreTakes(gameId, moveOutput, session, assignedColor);
        }
    }

    private void handleGameEnd(String gameId, GameState gameState) throws IOException {
        gameManager.setGameEndReason(gameId, false);
        messageSender.broadcastGameEnd(sessionManager.getSessionsByGameId(gameId),
                gameState, sessionManager.getColorAssignments(gameId));
    }

    private void handleMoreTakes(String gameId, MoveOutput moveOutput, WebSocketSession session, String assignedColor) throws IOException {
        PossibilitiesOutput moves = gameManager.getPossibleMoves(
                new PossibilitiesInput(gameId,
                        moveOutput.getMove().getToRow(),
                        moveOutput.getMove().getToCol()),
                session
        );
        messageSender.sendMessage(session, assignedColor, moves);
    }
}
