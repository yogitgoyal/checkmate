package model;

public class Rook extends Piece {
    public Rook(Color color) { super(color, PieceType.ROOK); }

    @Override
    public boolean isValidMove(int fr, int fc, int tr, int tc, Piece[][] b) {
        if (!basicOk(fr, fc, tr, tc, b)) return false;
        if (fr != tr && fc != tc) return false;
        return pathClear(fr, fc, tr, tc, b);
    }
}
