package pw.checkers.utils;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static final int AMOUNT_OF_PIECES = 12;
    public static final int BOARD_SIZE = 8;
    public static final List<int[]> DIRECTIONS_PAWN_WHITE = Arrays.asList(new int[]{-1, 1}, new int[]{-1, -1});
    public static final List<int[]> DIRECTIONS_PAWN_BLACK = Arrays.asList(new int[]{1, 1}, new int[]{1, -1});
    public static final List<int[]> DIRECTIONS_KING = Arrays.asList(new int[]{1, 1}, new int[]{1, -1}, new int[]{-1, 1}, new int[]{-1, -1});

}
