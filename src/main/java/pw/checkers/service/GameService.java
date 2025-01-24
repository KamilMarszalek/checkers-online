package pw.checkers.service;

import org.springframework.stereotype.Service;
import pw.checkers.pojo.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.abs;

@Service
public class GameService {
    private final Map<String, GameState> games = new ConcurrentHashMap<>();

    public GameState createGame() {
        String newGameId = UUID.randomUUID().toString();
        GameState gameState = new GameState();
        gameState.setGameId(newGameId);
        initializeBoard(gameState);
        gameState.setCurrentPlayer("white");
        gameState.setWinner(null);
        gameState.setFinished(false);
        games.put(newGameId, gameState);
        return gameState;
    }

    private void initializeBoard(GameState gameState) {
        Piece[][] board = new Piece[8][8];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    board[row][col] = new Piece(PieceColor.BLACK, PieceType.PAWN);
                }
            }
        }
        for (int row = 5; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    board[row][col] = new Piece(PieceColor.WHITE, PieceType.PAWN);
                }
            }
        }
        gameState.setBoard(board);
    }

    public GameState getGame(String gameId) {
        return games.get(gameId);
    }

    private boolean validateMove(GameState gameState, Move move) {
        // TODO move validation
        return true;
    }

    private void doTake(Piece[][] board, Move move) {
        if (abs(move.getFromColumn() - move.getToColumn()) > 1 && abs(move.getFromRow() - move.getToRow()) > 1) {
            int opponentRow = (move.getToRow() + move.getFromRow()) / 2;
            int opponentCol = (move.getToColumn() + move.getFromColumn()) / 2;
            board[opponentRow][opponentCol] = null;
        }
    }

    private boolean hasMoreTakes(GameState gameState, Move move) {
        Piece[][] board = gameState.getBoard();
        if (abs(move.getFromColumn() - move.getToColumn()) > 1 && abs(move.getFromRow() - move.getToRow()) > 1 ) {
            if (gameState.getCurrentPlayer().equals("white")) {
                //TODO find next take
                return true;
            }
            else {
                //TODO find next take
                return true;
            }
        }
        return false;
    }

    public GameState makeMove(String gameId, Move move) {
        GameState gameState = getGame(gameId);
        if (gameState == null || gameState.isFinished()){
            return null;
        }
        boolean b = validateMove(gameState, move);
        if (!b) {
            return null;
        };
        Piece[][] board = gameState.getBoard();
        Piece pawn = board[move.getFromRow()][move.getFromColumn()];
        board[move.getToRow()][move.getToColumn()] = pawn;
        board[move.getFromRow()][move.getFromColumn()] = null;
        doTake(board, move);
        if (hasMoreTakes(gameState, move)) {
            return gameState;
        }
        if (gameState.getCurrentPlayer().equals("white")) {
            gameState.setCurrentPlayer("black");
        } else {
            gameState.setCurrentPlayer("white");
        }
        return gameState;
    }
}
