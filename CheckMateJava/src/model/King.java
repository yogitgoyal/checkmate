package model;

/** Castling yahan nahi, GameEngine mein handle hoti hai (kyunki usse board state chahiye). */
public class King extends Piece {
    public King(Color color) { super(color, PieceType.KING); }

    @Override
    public boolean isValidMove(int fr, int fc, int tr, int tc, Piece[][] b) {
        if (!basicOk(fr, fc, tr, tc, b)) return false;
        return Math.abs(tr - fr) <= 1 && Math.abs(tc - fc) <= 1;
    }
}
