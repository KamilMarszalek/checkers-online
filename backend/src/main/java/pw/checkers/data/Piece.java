package pw.checkers.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pw.checkers.data.enums.PieceColor;
import pw.checkers.data.enums.PieceType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Piece implements Cloneable {
    private PieceColor color;
    private PieceType type;

    @Override
    public String toString() {
        return "Piece [color=" + color + ", type=" + type + "]";
    }

    @Override
    public Piece clone() throws CloneNotSupportedException {
        try {
            return (Piece) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new CloneNotSupportedException();
        }
    }
}
