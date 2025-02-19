package pw.checkers.service;

import pw.checkers.data.GameState;
import pw.checkers.message.Move;
import pw.checkers.message.MoveOutput;
import pw.checkers.message.PossibleMoves;

public interface GameService {
    GameState createGame();
    void deleteGame(String id);
    GameState getGame(String gameId);
    MoveOutput makeMove(String gameId, Move move, String currentTurn);
    PossibleMoves getPossibleMoves(GameState gameState, int row, int col);
}
