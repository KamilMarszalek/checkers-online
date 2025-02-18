package pw.checkers.service;

import pw.checkers.data.GameState;
import pw.checkers.messages.Move;
import pw.checkers.messages.MoveOutput;
import pw.checkers.messages.PossibleMoves;

public interface GameService {
    GameState createGame();
    GameState getGame(String gameId);
    MoveOutput makeMove(String gameId, Move move, String currentTurn);
    PossibleMoves getPossibleMoves(GameState gameState, int row, int col);
}
