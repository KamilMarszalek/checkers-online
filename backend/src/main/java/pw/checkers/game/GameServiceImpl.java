package pw.checkers.game;

import org.springframework.stereotype.Service;
import pw.checkers.data.GameState;
import pw.checkers.data.Piece;
import pw.checkers.data.enums.PieceColor;
import pw.checkers.message.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static pw.checkers.utils.Constants.*;

@Service
public class GameServiceImpl implements GameService{
    private final Map<String, GameState> games = new ConcurrentHashMap<>();
    private final BoardManager boardManager = new BoardManager();

    @Override
    public GameState createGame() {
        String newGameId = UUID.randomUUID().toString();
        GameState gameState = new GameState();
        gameState.setWhitePiecesLeft(AMOUNT_OF_PIECES);
        gameState.setBlackPiecesLeft(AMOUNT_OF_PIECES);
        gameState.setNoCapturesCounter(0);
        gameState.setGameId(newGameId);
        gameState.setNumberOfPositions(new HashMap<>());
        boardManager.initializeBoard(gameState);
        gameState.setCurrentPlayer(PieceColor.WHITE);
        gameState.setWinner(null);
        gameState.setFinished(false);
        games.put(newGameId, gameState);
        return gameState;
    }

    @Override
    public void deleteGame(String id) {
        games.remove(id);
    }


    @Override
    public GameState getGame(String gameId) {
        return games.get(gameId);
    }

    private boolean validateMove(GameState gameState, Move move) {
        Piece[][] board = gameState.getBoard();
        Piece piece = board[move.getFromRow()][move.getFromCol()];
        if (piece == null) return false;

        if (!piece.getColor().equals(gameState.getCurrentPlayer())) {
            return false;
        }

        if (gameState.getLastCaptureCol() != null && gameState.getLastCaptureRow() != null) {
            if (move.getFromRow() != gameState.getLastCaptureRow() || move.getFromCol() != gameState.getLastCaptureCol()) {
                return false;
            }
        }
        PossibleMoves pm = getPossibleMoves(gameState, move.getFromRow(), move.getFromCol());
        return pm.getMoves().contains(new MoveHelper(move.getToRow(), move.getToCol()));
    }






    private PieceColor mapStringToColor(String color) {
        return switch (color) {
            case "black" -> PieceColor.BLACK;
            case "white" -> PieceColor.WHITE;
            default -> null;
        };
    }

    @Override
    public MoveOutput makeMove(String gameId, Move move, String currentTurn) {
        PieceColor playerColor = mapStringToColor(currentTurn);
        MoveOutput response = new MoveOutput();
        response.setMove(move);
        GameState gameState = getGame(gameId);
        if (gameState == null || gameState.isFinished()){
            return null;
        }
        if (!gameState.getCurrentPlayer().equals(playerColor)) {
            return null;
        }
        boolean b = validateMove(gameState, move);
        if (!b) {
            return null;
        }
        return boardManager.makeMove(gameState, response, playerColor);
    }

    @Override
    public PossibleMoves getPossibleMoves(GameState gameState, int row, int col) {
        return  boardManager.getPossibleMoves(gameState, row, col);
    }
}
