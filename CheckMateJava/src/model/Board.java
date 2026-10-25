package model;

public class Board {
    private final Piece[][] grid = new Piece[8][8];

    /** Khaali board. */
    public Board() { }

    public static Board standard() {
        Board b = new Board();
        PieceType[] back = {PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK};
        for (int c = 0; c < 8; c++) {
            b.grid[0][c] = Piece.create(back[c], Color.BLACK);
            b.grid[1][c] = Piece.create(PieceType.PAWN, Color.BLACK);
            b.grid[6][c] = Piece.create(PieceType.PAWN, Color.WHITE);
            b.grid[7][c] = Piece.create(back[c], Color.WHITE);
        }
        return b;
    }

    public Piece[][] grid() { return grid; }
    public Piece get(int r, int c) { return grid[r][c]; }
    public void set(int r, int c, Piece p) { grid[r][c] = p; }
    public static boolean inBounds(int r, int c) { return r >= 0 && r < 8 && c >= 0 && c < 8; }

    /** "e2" -> {6, 4} */
    public static int[] sq(String s) {
        return new int[]{8 - (s.charAt(1) - '0'), s.charAt(0) - 'a'};
    }

    public static String name(int r, int c) {
        return "" + (char) ('a' + c) + (8 - r);
    }

    public int[] findKing(Color color) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null && p.getType() == PieceType.KING && p.getColor() == color) return new int[]{r, c};
            }
        return null;
    }

    public void print() {
        for (int r = 0; r < 8; r++) {
            System.out.print((8 - r) + " ");
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                char ch = p == null ? '.' : p.getType().getLetter();
                if (p != null && p.getColor() == Color.BLACK) ch = Character.toLowerCase(ch);
                System.out.print(ch + " ");
            }
            System.out.println();
        }
        System.out.println("  a b c d e f g h");
    }
}
