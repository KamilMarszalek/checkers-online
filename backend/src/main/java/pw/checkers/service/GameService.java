package pw.checkers.service;

import pw.checkers.pojo.GameState;
import pw.checkers.pojo.Move;
import pw.checkers.pojo.MoveOutput;
import pw.checkers.pojo.PossibleMoves;

public interface GameService {
    GameState createGame();
    GameState getGame(String gameId);
    MoveOutput makeMove(String gameId, Move move, String currentTurn);
    PossibleMoves getPossibleMoves(GameState gameState, int row, int col);
}
