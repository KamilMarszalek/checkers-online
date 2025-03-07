package pw.checkers.data.enums;

public enum GameEndReason {
    NO_PIECES("noPieces"),
    NO_MOVES("noMoves"),
    FIFTY_MOVES("fiftyMoves),
    THREEFOLD_REPETITION("threefoldRepetition"),
    RESIGNATION("Resignation");

    GameEndConditions(String value) {}

}
