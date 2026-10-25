package model;

public class Bishop extends Piece {
    public Bishop(Color color) { super(color, PieceType.BISHOP); }

    @Override
    public boolean isValidMove(int fr, int fc, int tr, int tc, Piece[][] b) {
        if (!basicOk(fr, fc, tr, tc, b)) return false;
        if (Math.abs(tr - fr) != Math.abs(tc - fc)) return false;
        return pathClear(fr, fc, tr, tc, b);
    }
}
