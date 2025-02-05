package pw.checkers.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PossibleMoves {
    private List<MoveInput> moves;

    public PossibleMoves() {
        this.moves = new ArrayList<>();
    }
}
