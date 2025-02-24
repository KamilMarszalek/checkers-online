package pw.checkers.game;

import org.springframework.stereotype.Service;
import pw.checkers.data.GameState;
import pw.checkers.data.enums.PieceColor;
import pw.checkers.message.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static pw.checkers.utils.Constants.*;

@Service
public class GameServiceImpl implements GameService{
    private final Map<String, GameState> games = new ConcurrentHashMap<>();
    private final BoardManager boardManager;
    private final MoveValidator moveValidator;

    public GameServiceImpl(BoardManager boardManager, MoveValidator moveValidator) {
        this.boardManager = boardManager;
        this.moveValidator = moveValidator;
    }

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

    @Override
    public MoveOutput makeMove(String gameId, Move move, String currentTurn) {
        MoveOutput response = new MoveOutput();
        response.setMove(move);
        GameState gameState = getGame(gameId);
        if (!moveValidator.validateMove(gameState, move)) {
            return null;
        }
        return boardManager.makeMove(gameState, response);
    }

    @Override
    public PossibleMoves getPossibleMoves(GameState gameState, int row, int col) {
        return  boardManager.getPossibleMoves(gameState, row, col);
    }
}
