package test;

import engine.GameEngine;
import model.*;

/** Simple test runner (JUnit ki zarurat nahi). Run: java -cp out test.EngineTests */
public class EngineTests {
    static int passed = 0, failed = 0;

    static void check(String name, boolean cond) {
        if (cond) { passed++; System.out.println("PASS  " + name); }
        else { failed++; System.out.println("FAIL  " + name); }
    }

    static void play(GameEngine e, String... moves) {
        for (String m : moves) {
            if (!e.makeMove(m.substring(0, 2), m.substring(2, 4)))
                throw new RuntimeException("Illegal move in test: " + m);
        }
    }

    static void put(Board b, String sq, PieceType t, Color c) {
        int[] p = Board.sq(sq);
        b.set(p[0], p[1], Piece.create(t, c));
    }

    public static void main(String[] args) {
        // 1. starting position mein 20 legal moves
        GameEngine e = new GameEngine();
        int count = 0;
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = e.getBoard().get(r, c);
                if (p != null && p.getColor() == Color.WHITE) count += e.legalMovesFrom(r, c).size();
            }
        check("Start position: 20 legal moves", count == 20);

        // 2. illegal moves reject hon
        e = new GameEngine();
        check("e2e5 illegal (pawn 3 step)", !e.makeMove("e2", "e5"));
        check("Black piece pehle nahi chal sakta", !e.makeMove("e7", "e5"));
        check("Knight b1-c3 legal", e.makeMove("b1", "c3"));

        // 3. Fool's mate
        e = new GameEngine();
        play(e, "f2f3", "e7e5", "g2g4", "d8h4");
        check("Fool's Mate = CHECKMATE", e.getStatus() == GameEngine.Status.CHECKMATE);
        check("Fool's Mate result 0-1", e.result().equals("0-1"));

        // 4. Scholar's mate
        e = new GameEngine();
        play(e, "e2e4", "e7e5", "d1h5", "b8c6", "f1c4", "g8f6", "h5f7");
        check("Scholar's Mate = CHECKMATE", e.getStatus() == GameEngine.Status.CHECKMATE);
        check("SAN ends with #", e.getHistory().get(e.getHistory().size() - 1).san.equals("Qxf7#"));

        // 5. Stalemate
        Board b = new Board();
        put(b, "a8", PieceType.KING, Color.BLACK);
        put(b, "c7", PieceType.QUEEN, Color.WHITE);
        put(b, "b6", PieceType.KING, Color.WHITE);
        e = new GameEngine(b, Color.BLACK);
        check("Stalemate detect", e.getStatus() == GameEngine.Status.STALEMATE);

        // 6. Pinned piece cannot move
        b = new Board();
        put(b, "e1", PieceType.KING, Color.WHITE);
        put(b, "e2", PieceType.ROOK, Color.WHITE);
        put(b, "e8", PieceType.ROOK, Color.BLACK);
        put(b, "a8", PieceType.KING, Color.BLACK);
        e = new GameEngine(b, Color.WHITE);
        check("Pinned rook sideways illegal", !e.makeMove("e2", "d2"));
        check("Pinned rook along pin legal", e.makeMove("e2", "e5"));

        // 7. Castling
        b = new Board();
        put(b, "e1", PieceType.KING, Color.WHITE);
        put(b, "h1", PieceType.ROOK, Color.WHITE);
        put(b, "a1", PieceType.ROOK, Color.WHITE);
        put(b, "e8", PieceType.KING, Color.BLACK);
        e = new GameEngine(b, Color.WHITE);
        check("Kingside castle legal", e.makeMove("e1", "g1"));
        check("King on g1", e.getBoard().get(7, 6) != null && e.getBoard().get(7, 6).getType() == PieceType.KING);
        check("Rook on f1", e.getBoard().get(7, 5) != null && e.getBoard().get(7, 5).getType() == PieceType.ROOK);
        check("SAN O-O", e.getHistory().get(0).san.equals("O-O"));

        // 8. Castling through attack not allowed
        b = new Board();
        put(b, "e1", PieceType.KING, Color.WHITE);
        put(b, "h1", PieceType.ROOK, Color.WHITE);
        put(b, "f8", PieceType.ROOK, Color.BLACK);
        put(b, "a8", PieceType.KING, Color.BLACK);
        e = new GameEngine(b, Color.WHITE);
        check("Castle through attacked square illegal", !e.makeMove("e1", "g1"));

        // 9. En passant
        e = new GameEngine();
        play(e, "e2e4", "a7a6", "e4e5", "d7d5", "e5d6");
        check("En passant capture: d5 pawn removed", e.getBoard().get(3, 3) == null);
        check("En passant: white pawn on d6", e.getBoard().get(2, 3) != null
                && e.getBoard().get(2, 3).getColor() == Color.WHITE);

        // 10. Promotion
        b = new Board();
        put(b, "a7", PieceType.PAWN, Color.WHITE);
        put(b, "e1", PieceType.KING, Color.WHITE);
        put(b, "e8", PieceType.KING, Color.BLACK);
        e = new GameEngine(b, Color.WHITE);
        check("Promotion to Knight", e.makeMove("a7", "a8", PieceType.KNIGHT));
        check("a8 is Knight", e.getBoard().get(0, 0).getType() == PieceType.KNIGHT);
        check("Promotion SAN", e.getHistory().get(0).san.equals("a8=N"));

        // 11. Insufficient material
        b = new Board();
        put(b, "e1", PieceType.KING, Color.WHITE);
        put(b, "e8", PieceType.KING, Color.BLACK);
        e = new GameEngine(b, Color.WHITE);
        check("K vs K = draw", e.getStatus() == GameEngine.Status.DRAW_INSUFFICIENT);

        // 12. PGN
        e = new GameEngine();
        play(e, "e2e4", "e7e5", "g1f3");
        check("PGN contains moves", e.toPgn("A", "B").contains("1. e4 e5 2. Nf3"));

        System.out.println("\nPassed: " + passed + "  Failed: " + failed);
        if (failed > 0) System.exit(1);
    }
}
