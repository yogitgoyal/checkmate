package engine;

import model.PieceType;

/** Ek chala hua move ka record (history, log aur PGN ke liye). */
public class Move {
    public final int fromRow, fromCol, toRow, toCol;
    public final PieceType moved;
    public final PieceType captured;
    public final PieceType promotion;
    public final boolean castling, enPassant;
    public final String san;

    public Move(int fr, int fc, int tr, int tc, PieceType moved, PieceType captured,
                PieceType promotion, boolean castling, boolean enPassant, String san) {
        this.fromRow = fr; this.fromCol = fc; this.toRow = tr; this.toCol = tc;
        this.moved = moved; this.captured = captured; this.promotion = promotion;
        this.castling = castling; this.enPassant = enPassant; this.san = san;
    }

    @Override
    public String toString() { return san; }
}
