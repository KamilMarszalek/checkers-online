package pw.checkers.data.enums;

public enum GameEndConditions {
    NO_PIECES("NO_PIECES"),
    NO_MOVES("NO_MOVES"),
    FIFTY_MOVES_VIOLATION("FIFTY_MOVES_VIOLATION"),
    THREEFOLD_REPETITION("THREE_FOLD_REPETITION"),
    RESIGNATION("RESIGNATION");

    GameEndConditions(String value) {}

}
