package model;

public enum PieceType {
    KING('K', "\u2654", "\u265A"),
    QUEEN('Q', "\u2655", "\u265B"),
    ROOK('R', "\u2656", "\u265C"),
    BISHOP('B', "\u2657", "\u265D"),
    KNIGHT('N', "\u2658", "\u265E"),
    PAWN('P', "\u2659", "\u265F");

    private final char letter;
    private final String whiteSymbol;
    private final String blackSymbol;

    PieceType(char letter, String whiteSymbol, String blackSymbol) {
        this.letter = letter;
        this.whiteSymbol = whiteSymbol;
        this.blackSymbol = blackSymbol;
    }

    public char getLetter() { return letter; }

    public String getSymbol(Color c) {
        return c == Color.WHITE ? whiteSymbol : blackSymbol;
    }
}
