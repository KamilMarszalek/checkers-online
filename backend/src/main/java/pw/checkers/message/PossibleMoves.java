package pw.checkers.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PossibleMoves {
    private List<MoveHelper> moves;

    public PossibleMoves() {
        this.moves = new ArrayList<>();
    }
}
