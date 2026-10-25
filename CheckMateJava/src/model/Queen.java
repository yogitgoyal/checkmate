package model;

public class Queen extends Piece {
    public Queen(Color color) { super(color, PieceType.QUEEN); }

    @Override
    public boolean isValidMove(int fr, int fc, int tr, int tc, Piece[][] b) {
        if (!basicOk(fr, fc, tr, tc, b)) return false;
        boolean straight = (fr == tr || fc == tc);
        boolean diagonal = Math.abs(tr - fr) == Math.abs(tc - fc);
        if (!straight && !diagonal) return false;
        return pathClear(fr, fc, tr, tc, b);
    }
}
