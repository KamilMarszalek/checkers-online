package pw.checkers.data.enums;

import lombok.Getter;

@Getter
public enum Color {
    WHITE("white"),
    BLACK("black");

    private final String value;

    Color(String value) {
        this.value = value;
    }
}
