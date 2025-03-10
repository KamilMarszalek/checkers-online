package pw.checkers.game;

import pw.checkers.data.GameState;
import pw.checkers.message.Move;
import pw.checkers.message.MoveOutputMessage;
import pw.checkers.message.PossibilitiesOutputMessage;

public interface GameService {
    GameState createGame();
    void deleteGame(String id);
    GameState getGame(String gameId);
    MoveOutputMessage makeMove(String gameId, Move move, String currentTurn);
    PossibilitiesOutputMessage getPossibleMoves(GameState gameState, int row, int col);
}
