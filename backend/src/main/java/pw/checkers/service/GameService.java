package pw.checkers.service;

import org.springframework.stereotype.Service;
import pw.checkers.pojo.*;

import java.util.ArrayList;
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

    private void promotePiece(Piece pawn, Move move, GameState gameState) {
        if ((gameState.getCurrentPlayer().equals("white") && move.getToRow() == 0) || (gameState.getCurrentPlayer().equals("black") && move.getToColumn() == 7)) {
            pawn.setType(PieceType.KING);
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
        }
        Piece[][] board = gameState.getBoard();
        Piece pawn = board[move.getFromRow()][move.getFromColumn()];
        promotePiece(pawn, move, gameState);
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

    public PossibleMoves getPossibleMoves(GameState gameState, int row, int col) {
        Piece[][] board = gameState.getBoard();
        Piece pawn = board[row][col];
        if (pawn.getType().equals(PieceType.KING)) {
            return getPossibleMovesForKing(gameState, row, col);
        }
        return getPossibleMovesForPawn(gameState, row, col);
    }

    private PossibleMoves getPossibleMovesForKing(GameState gameState, int row, int col) {
        PossibleMoves possibleMoves = new PossibleMoves();
        Piece[][] board = gameState.getBoard();
        //TODO
        return possibleMoves;
    }

    private PossibleMoves getPossibleMovesForPawn(GameState gameState, int row, int col) {
        PossibleMoves possibleMoves = new PossibleMoves();
        possibleMoves.setMoves(new ArrayList<>());
        Piece[][] board = gameState.getBoard();
        if (findTakesPawn(possibleMoves, board, row, col)) {
            return possibleMoves;
        }
        return possibleMoves;
    }

    private boolean findTakesPawn(PossibleMoves possibleMoves, Piece[][] board, int i, int j) {
        Piece pawn = board[i][j];
        if (pawn.getColor().equals(PieceColor.BLACK)) {
            if (i + 2 < board.length && j + 2 < board[i].length) {
                if (board[i + 1][j + 1] != null) {
                    if (board[i + 1][j + 1].getColor().equals(PieceColor.WHITE) && board[i + 2][j + 2] == null) {
                        possibleMoves.getMoves().add(new Move(i, j, i + 2, j + 2));
                    }
                }
            }

            if (i + 2 < board.length && j - 2 >= 0) {
                if ( board[i + 1][j - 1] != null) {
                    if (board[i + 1][j - 1].getColor().equals(PieceColor.WHITE) && board[i + 2][j - 2] == null) {
                        possibleMoves.getMoves().add(new Move(i, j, i + 2, j - 2));
                    }
                }
            }
        }
        if (pawn.getColor().equals(PieceColor.WHITE)) {
            if (i - 2 >= 0 && j + 2 < board[i].length) {
                if (board[i - 1][j + 1] != null) {
                    if (board[i - 1][j + 1].getColor().equals(PieceColor.BLACK) && board[i - 2][j + 2] == null) {
                        possibleMoves.getMoves().add(new Move(i, j, i - 2, j + 2));
                    }
                }
            }

            if (i - 2 >= 0 && j - 2 >= 0) {
                if ( board[i - 1][j - 1] != null) {
                    if (board[i - 1][j - 1].getColor().equals(PieceColor.BLACK) && board[i - 2][j - 2] == null) {
                        possibleMoves.getMoves().add(new Move(i, j, i - 2, j - 2));
                    }
                }
            }
        }
        return !possibleMoves.getMoves().isEmpty();
    }
}
