package pw.checkers.data.enums;

import lombok.Getter;

@Getter
public enum GameEndReason {
    NO_PIECES("noPieces"),
    NO_MOVES("noMoves"),
    FIFTY_MOVES("fiftyMoves"),
    THREEFOLD_REPETITION("threefoldRepetition"),
    RESIGNATION("resignation");

    private final String value;

    GameEndReason(String value) {
        this.value = value;
    }

}
