package pw.checkers.game;

import pw.checkers.data.GameState;
import pw.checkers.message.Move;
import pw.checkers.message.MoveOutput;
import pw.checkers.message.PossibilitiesOutput;

public interface GameService {
    GameState createGame();
    void deleteGame(String id);
    GameState getGame(String gameId);
    MoveOutput makeMove(String gameId, Move move, String currentTurn);
    PossibilitiesOutput getPossibleMoves(GameState gameState, int row, int col);
}
