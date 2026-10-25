package model;

public class Knight extends Piece {
    public Knight(Color color) { super(color, PieceType.KNIGHT); }

    @Override
    public boolean isValidMove(int fr, int fc, int tr, int tc, Piece[][] b) {
        if (!basicOk(fr, fc, tr, tc, b)) return false;
        int dr = Math.abs(tr - fr), dc = Math.abs(tc - fc);
        return (dr == 2 && dc == 1) || (dr == 1 && dc == 2);
    }
}
