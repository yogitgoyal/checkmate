package model;

/** En passant aur promotion GameEngine mein handle hote hain. */
public class Pawn extends Piece {
    public Pawn(Color color) { super(color, PieceType.PAWN); }

    public int direction() { return color == Color.WHITE ? -1 : 1; }
    public int startRow() { return color == Color.WHITE ? 6 : 1; }

    @Override
    public boolean isValidMove(int fr, int fc, int tr, int tc, Piece[][] b) {
        if (tr < 0 || tr > 7 || tc < 0 || tc > 7) return false;
        int dir = direction();
        int dr = tr - fr, dc = tc - fc;
        if (dc == 0 && dr == dir) return b[tr][tc] == null;
        if (dc == 0 && dr == 2 * dir && fr == startRow()) {
            return b[fr + dir][fc] == null && b[tr][tc] == null;
        }
        if (Math.abs(dc) == 1 && dr == dir) {
            return b[tr][tc] != null && b[tr][tc].color != color;
        }
        return false;
    }

    @Override
    public boolean attacks(int fr, int fc, int tr, int tc, Piece[][] b) {
        return Math.abs(tc - fc) == 1 && (tr - fr) == direction();
    }
}
