package pw.checkers.service;

import org.springframework.stereotype.Service;
import pw.checkers.pojo.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.abs;

@Service
public class GameService {
    private final Map<String, GameState> games = new ConcurrentHashMap<>();

    public GameState createGame() {
        String newGameId = UUID.randomUUID().toString();
        GameState gameState = new GameState();
        gameState.setWhitePiecesLeft(12);
        gameState.setBlackPiecesLeft(12);
        gameState.setNoCapturesCounter(0);
        gameState.setGameId(newGameId);
        gameState.setNumberOfPositions(new HashMap<>());
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

    private boolean validateMove(GameState gameState, MoveInput move) {
        Piece[][] board = gameState.getBoard();
        Piece piece = board[move.getFromRow()][move.getFromColumn()];
        if (piece == null) return false;

        if (!colorMatchesCurrentPlayer(piece.getColor(), gameState.getCurrentPlayer())) {
            return false;
        }
        PossibleMoves pm = getPossibleMoves(gameState, move.getFromRow(), move.getFromColumn());
        return pm.getMoves().contains(move);
    }

    private boolean colorMatchesCurrentPlayer(PieceColor color, String currentPlayer) {
        if (color == PieceColor.WHITE && "white".equals(currentPlayer)) return true;
        return color == PieceColor.BLACK && "black".equals(currentPlayer);
    }

    private void doTake(GameState gameState, MoveOutput move) {
        Piece[][] board = gameState.getBoard();
        if (abs(move.getFromColumn() - move.getToColumn()) > 1 && abs(move.getFromRow() - move.getToRow()) > 1) {
            int opponentRow = (move.getToRow() + move.getFromRow()) / 2;
            int opponentCol = (move.getToColumn() + move.getFromColumn()) / 2;
            Piece capturedPiece = board[opponentRow][opponentCol];
            if (capturedPiece.getColor().equals(PieceColor.BLACK)) {
                gameState.setBlackPiecesLeft(gameState.getBlackPiecesLeft() - 1);
            } else if (capturedPiece.getColor().equals(PieceColor.WHITE)) {
                gameState.setWhitePiecesLeft(gameState.getWhitePiecesLeft() - 1);
            }
            board[opponentRow][opponentCol] = null;
            move.setCapturedRow(opponentRow);
            move.setCapturedCol(opponentCol);
            move.setCaptured(true);
            gameState.setNoCapturesCounter(0);
        }
    }

    private void promotePiece(Piece pawn, MoveInput move, GameState gameState) {
        if ((gameState.getCurrentPlayer().equals("white") && move.getToRow() == 0) || (gameState.getCurrentPlayer().equals("black") && move.getToRow() == 7)) {
            pawn.setType(PieceType.KING);
        }
    }

    private boolean hasMoreTakes(GameState gameState, MoveInput move) {
        Piece[][] board = gameState.getBoard();
        boolean isKing = board[move.getToRow()][move.getToColumn()].getType().equals(PieceType.KING);
        if (abs(move.getFromColumn() - move.getToColumn()) > 1 && abs(move.getFromRow() - move.getToRow()) > 1 ) {
            return findTakes(new PossibleMoves(), board, move.getToRow(), move.getToColumn(), isKing);
        }
        return false;
    }

    public MoveOutput makeMove(String gameId, MoveInput move) {
        MoveOutput response = new MoveOutput(move);
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
        gameState.setNoCapturesCounter(gameState.getNoCapturesCounter() + 1);
        doTake(gameState, response);
        if (hasMoreTakes(gameState, response)) {
            response.setHasMoreTakes(true);
            return response;
        }
        if (hasSomebodyWon(gameState)) {
            setWinner(gameState);
        }
        int posCounter = gameState.getNumberOfPositions().get(gameState.boardToString()) == null ? 0 : gameState.getNumberOfPositions().get(gameState.boardToString());
        gameState.getNumberOfPositions().put(gameState.boardToString(), posCounter + 1);
        if (isDraw(gameState) && gameState.getWinner() != null) {
            setDraw(gameState);
        }
        if (gameState.getCurrentPlayer().equals("white")) {
            gameState.setCurrentPlayer("black");
        } else {
            gameState.setCurrentPlayer("white");
        }
        return response;
    }

    private void setDraw(GameState gameState) {
        gameState.setFinished(true);
    }

    private boolean isDraw(GameState gameState) {
        String currentPlayer = gameState.getCurrentPlayer();
        String otherPlayer = gameState.getCurrentPlayer().equals("white") ? "black" : "white";
        if (!playerHasMoves(gameState, currentPlayer) && !playerHasMoves(gameState, otherPlayer)) {
            return true;
        }
        if (isFiftyMoveViolation(gameState)) {
            return true;
        }
        if (isPositionRepeatedThreeTimes(gameState)) {
            return true;
        }
        return false;
    }

    private boolean playerHasMoves(GameState gameState, String player) {
        for (int row = 0; row < gameState.getBoard().length; row++) {
            for (int column = 0; column < gameState.getBoard()[row].length; column++) {
                if (gameState.getBoard()[row][column] != null) {
                    if (colorMatchesCurrentPlayer(gameState.getBoard()[row][column].getColor(), player)) {
                        if (!getPossibleMoves(gameState, row, column).getMoves().isEmpty()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isPositionRepeatedThreeTimes(GameState gameState) {
        return gameState.getNumberOfPositions().get(gameState.boardToString()) >= 3;
    }

    private boolean isFiftyMoveViolation(GameState gameState) {
        return gameState.getNoCapturesCounter() >= 50;
    }

    private boolean hasSomebodyWon(GameState gameState) {
        return gameState.getBlackPiecesLeft() == 0 || gameState.getWhitePiecesLeft() == 0;
    }

    private void setWinner(GameState gameState) {
        if (gameState.getWhitePiecesLeft() > 0 && gameState.getBlackPiecesLeft() == 0) {
            gameState.setWinner("white");
            gameState.setFinished(true);
        } else if (gameState.getBlackPiecesLeft() > 0 && gameState.getWhitePiecesLeft() == 0) {
            gameState.setWinner("black");
            gameState.setFinished(true);
        }
    }

    public PossibleMoves getPossibleMoves(GameState gameState, int row, int col) {
        Piece[][] board = gameState.getBoard();
        Piece pawn = board[row][col];
        if (pawn == null) {
            return new PossibleMoves();
        }
        if (pawn.getType().equals(PieceType.KING)) {
            return getPossibleMovesHelper(gameState, row, col, true);
        }
        return getPossibleMovesHelper(gameState, row, col, false);
    }

    private boolean hasAnyCapture(GameState gameState, PieceColor color) {
        Piece[][] board = gameState.getBoard();

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                Piece p = board[row][col];
                if (p != null && p.getColor() == color) {
                    boolean isKing = (p.getType() == PieceType.KING);
                    PossibleMoves temp = new PossibleMoves();
                    temp.setMoves(new ArrayList<>());
                    if (findTakes(temp, board, row, col, isKing)) {
                        if (!temp.getMoves().isEmpty()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private PossibleMoves getPossibleMovesHelper(GameState gameState, int row, int col, boolean isKing) {
        PossibleMoves possibleMoves = new PossibleMoves();
        possibleMoves.setMoves(new ArrayList<>());
        Piece[][] board = gameState.getBoard();
        Piece piece = board[row][col];
        if (piece == null) {
            return possibleMoves;
        }
        PieceColor color = piece.getColor();
        boolean anyCaptureInColor = hasAnyCapture(gameState, color);
        if (anyCaptureInColor) {
            findTakes(possibleMoves, board, row, col, isKing);
        } else {
            findOtherMoves(possibleMoves, board, row, col, isKing);
        }
        return possibleMoves;
    }

    private void findOtherMoves(PossibleMoves possibleMoves,Piece[][] board, int row, int col, boolean isKing) {
        Piece pawn = board[row][col];
        PieceColor color = pawn.getColor();
        List<int[]> directions = (color == PieceColor.BLACK)
                ? Arrays.asList(new int[]{1,1}, new int[]{1,-1})
                : Arrays.asList(new int[]{-1,1}, new int[]{-1,-1});
        if (isKing) {
            directions = Arrays.asList(new int[]{1, 1}, new int[]{1, -1}, new int[]{-1, 1}, new int[]{-1, -1});
        }
        for (int[] direction : directions) {
            int deltaRow = direction[0];
            int deltaCol = direction[1];

            int landingRow = row + deltaRow;
            int landingCol = col + deltaCol;

            if (landingRow < 0 || landingRow >= board.length
                    || landingCol < 0 || landingCol >= board[0].length) {
                continue;
            }

            if (board[landingRow][landingCol] == null) {
                possibleMoves.getMoves().add(new MoveInput(row, col, landingRow, landingCol));
            }
        }
    }

    private boolean findTakes(PossibleMoves possibleMoves, Piece[][] board, int row, int col, boolean isKing) {
        Piece pawn = board[row][col];
        PieceColor color = pawn.getColor();

        PieceColor opponentColor = (color == PieceColor.BLACK) ? PieceColor.WHITE : PieceColor.BLACK;
        List<int[]> directions = (color == PieceColor.BLACK)
                ? Arrays.asList(new int[]{1,1}, new int[]{1,-1})
                : Arrays.asList(new int[]{-1,1}, new int[]{-1,-1});
        if (isKing) {
            directions = Arrays.asList(new int[]{1,1}, new int[]{-1,1}, new int[]{-1,-1}, new int[]{1,-1});
        }

        for (int[] direction : directions) {
            int deltaRow = direction[0];
            int deltaCol = direction[1];

            int middleRow = row + deltaRow;
            int middleCol = col + deltaCol;

            int landingRow = row + 2 * deltaRow;
            int landingCol = col + 2 * deltaCol;

            if (landingRow < 0 || landingRow >= board.length
                    || landingCol < 0 || landingCol >= board[0].length) {
                continue;
            }

            if (board[middleRow][middleCol] != null
                    && board[middleRow][middleCol].getColor() == opponentColor
                    && board[landingRow][landingCol] == null) {
                possibleMoves.getMoves().add(new MoveInput(row, col, landingRow, landingCol));
            }
        }

        return !possibleMoves.getMoves().isEmpty();
    }

}
