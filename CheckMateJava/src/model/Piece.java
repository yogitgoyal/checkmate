package model;

/** Abstract base class: har piece ki movement rule isValidMove() mein hoti hai (pseudo-legal). */
public abstract class Piece {
    protected final Color color;
    protected final PieceType type;
    protected boolean hasMoved = false;

    protected Piece(Color color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public Color getColor() { return color; }
    public PieceType getType() { return type; }
    public boolean hasMoved() { return hasMoved; }
    public void setMoved(boolean moved) { this.hasMoved = moved; }
    public String getSymbol() { return type.getSymbol(color); }

    /** Sirf piece ki geometry + blocking. King safety yahan check nahi hoti (wo GameEngine karta hai). */
    public abstract boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board);

    /** Kya ye piece target square ko attack karta hai? (Pawn ke liye alag hai) */
    public boolean attacks(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        return isValidMove(fromRow, fromCol, toRow, toCol, board);
    }

    /** Common basic checks: board ke andar, same square nahi, apne piece pe nahi. */
    protected boolean basicOk(int fr, int fc, int tr, int tc, Piece[][] b) {
        if (tr < 0 || tr > 7 || tc < 0 || tc > 7) return false;
        if (fr == tr && fc == tc) return false;
        Piece target = b[tr][tc];
        return target == null || target.color != this.color;
    }

    /** Beech ke squares khaali hain ya nahi (Rook, Bishop, Queen use karte hain). */
    protected static boolean pathClear(int fr, int fc, int tr, int tc, Piece[][] b) {
        int dr = Integer.signum(tr - fr);
        int dc = Integer.signum(tc - fc);
        int r = fr + dr, c = fc + dc;
        while (r != tr || c != tc) {
            if (b[r][c] != null) return false;
            r += dr;
            c += dc;
        }
        return true;
    }

    public static Piece create(PieceType type, Color color) {
        switch (type) {
            case ROOK:   return new Rook(color);
            case KNIGHT: return new Knight(color);
            case BISHOP: return new Bishop(color);
            case QUEEN:  return new Queen(color);
            case KING:   return new King(color);
            default:     return new Pawn(color);
        }
    }
}
