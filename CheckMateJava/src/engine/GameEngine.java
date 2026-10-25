package engine;

import model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Game ka dimaag: turn, legal moves, check/checkmate/stalemate, special moves, move history. */
public class GameEngine {

    public enum Status { ONGOING, CHECK, CHECKMATE, STALEMATE, DRAW_INSUFFICIENT, DRAW_FIFTY_MOVE }

    private final Board board;
    private Color turn;
    private final List<Move> history = new ArrayList<>();
    private int[] enPassant = null;
    private int halfmoveClock = 0;
    private Status status = Status.ONGOING;

    public GameEngine() {
        this(Board.standard(), Color.WHITE);
    }

    public GameEngine(Board board, Color turn) {
        this.board = board;
        this.turn = turn;
        updateStatus();
    }

    public Board getBoard() { return board; }
    public Color getTurn() { return turn; }
    public Status getStatus() { return status; }
    public List<Move> getHistory() { return history; }
    public boolean isGameOver() {
        return status != Status.ONGOING && status != Status.CHECK;
    }

    // ---------------- attack / check ----------------
    public boolean isSquareAttacked(int r, int c, Color by) {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                Piece p = board.get(i, j);
                if (p != null && p.getColor() == by && p.attacks(i, j, r, c, board.grid())) return true;
            }
        return false;
    }

    public boolean isInCheck(Color color) {
        int[] k = board.findKing(color);
        return k != null && isSquareAttacked(k[0], k[1], color.opposite());
    }

    // ---------------- special move detection ----------------
    private boolean isCastle(int fr, int fc, int tr, int tc) {
        Piece p = board.get(fr, fc);
        if (p == null || p.getType() != PieceType.KING || p.hasMoved()) return false;
        int home = p.getColor() == Color.WHITE ? 7 : 0;
        if (fr != home || fc != 4 || tr != home || Math.abs(tc - fc) != 2) return false;
        int rookCol = tc > fc ? 7 : 0;
        Piece rook = board.get(home, rookCol);
        if (rook == null || rook.getType() != PieceType.ROOK || rook.getColor() != p.getColor() || rook.hasMoved())
            return false;
        for (int c = Math.min(fc, rookCol) + 1; c < Math.max(fc, rookCol); c++)
            if (board.get(home, c) != null) return false;
        Color enemy = p.getColor().opposite();
        int step = tc > fc ? 1 : -1;
        return !isSquareAttacked(home, fc, enemy)
                && !isSquareAttacked(home, fc + step, enemy)
                && !isSquareAttacked(home, tc, enemy);
    }

    private boolean isEnPassant(int fr, int fc, int tr, int tc) {
        Piece p = board.get(fr, fc);
        if (p == null || p.getType() != PieceType.PAWN || enPassant == null) return false;
        Pawn pawn = (Pawn) p;
        return tr == enPassant[0] && tc == enPassant[1] && Math.abs(tc - fc) == 1
                && tr - fr == pawn.direction() && board.get(tr, tc) == null
                && board.get(fr, tc) != null && board.get(fr, tc).getType() == PieceType.PAWN
                && board.get(fr, tc).getColor() != p.getColor();
    }

    // ---------------- legal move generation ----------------
    private boolean isPseudoLegal(int fr, int fc, int tr, int tc) {
        Piece p = board.get(fr, fc);
        if (p == null || !Board.inBounds(tr, tc)) return false;
        return p.isValidMove(fr, fc, tr, tc, board.grid()) || isCastle(fr, fc, tr, tc) || isEnPassant(fr, fc, tr, tc);
    }

    /** Move simulate karo, dekho apna King check mein to nahi, phir undo. */
    private boolean leavesKingInCheck(int fr, int fc, int tr, int tc) {
        Piece p = board.get(fr, fc);
        Piece captured = board.get(tr, tc);
        boolean ep = isEnPassant(fr, fc, tr, tc);
        Piece epPiece = null;
        if (ep) { epPiece = board.get(fr, tc); board.set(fr, tc, null); }
        board.set(tr, tc, p);
        board.set(fr, fc, null);
        boolean inCheck = isInCheck(p.getColor());
        board.set(fr, fc, p);
        board.set(tr, tc, captured);
        if (ep) board.set(fr, tc, epPiece);
        return inCheck;
    }

    public boolean isLegalMove(int fr, int fc, int tr, int tc) {
        return isPseudoLegal(fr, fc, tr, tc) && !leavesKingInCheck(fr, fc, tr, tc);
    }

    public List<int[]> legalMovesFrom(int r, int c) {
        List<int[]> res = new ArrayList<>();
        Piece p = board.get(r, c);
        if (p == null) return res;
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (isLegalMove(r, c, i, j)) res.add(new int[]{i, j});
        return res;
    }

    public boolean hasAnyLegalMove(Color color) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = board.get(r, c);
                if (p != null && p.getColor() == color && !legalMovesFrom(r, c).isEmpty()) return true;
            }
        return false;
    }

    // ---------------- making a move ----------------
    public boolean makeMove(String from, String to) {
        return makeMove(from, to, PieceType.QUEEN);
    }

    public boolean makeMove(String from, String to, PieceType promo) {
        int[] f = Board.sq(from), t = Board.sq(to);
        return makeMove(f[0], f[1], t[0], t[1], promo);
    }

    public boolean makeMove(int fr, int fc, int tr, int tc, PieceType promo) {
        if (isGameOver()) return false;
        Piece p = board.get(fr, fc);
        if (p == null || p.getColor() != turn) return false;
        if (!isLegalMove(fr, fc, tr, tc)) return false;

        boolean castle = isCastle(fr, fc, tr, tc);
        boolean ep = isEnPassant(fr, fc, tr, tc);
        Piece captured = ep ? board.get(fr, tc) : board.get(tr, tc);
        boolean promotes = p.getType() == PieceType.PAWN && (tr == 0 || tr == 7);
        if (promo == null || promo == PieceType.KING || promo == PieceType.PAWN) promo = PieceType.QUEEN;

        String san = buildSan(fr, fc, tr, tc, p, captured != null, castle, promotes ? promo : null);

        if (ep) board.set(fr, tc, null);
        if (castle) {
            int rookCol = tc > fc ? 7 : 0;
            Piece rook = board.get(fr, rookCol);
            board.set(fr, rookCol, null);
            board.set(fr, (fc + tc) / 2, rook);
            rook.setMoved(true);
        }
        board.set(fr, fc, null);
        Piece placed = promotes ? Piece.create(promo, p.getColor()) : p;
        placed.setMoved(true);
        board.set(tr, tc, placed);

        enPassant = (p.getType() == PieceType.PAWN && Math.abs(tr - fr) == 2)
                ? new int[]{(fr + tr) / 2, fc} : null;
        halfmoveClock = (p.getType() == PieceType.PAWN || captured != null) ? 0 : halfmoveClock + 1;
        turn = turn.opposite();
        updateStatus();

        if (status == Status.CHECKMATE) san += "#";
        else if (status == Status.CHECK) san += "+";

        history.add(new Move(fr, fc, tr, tc, p.getType(), captured == null ? null : captured.getType(),
                promotes ? promo : null, castle, ep, san));
        return true;
    }

    // ---------------- status ----------------
    private void updateStatus() {
        boolean check = isInCheck(turn);
        if (!hasAnyLegalMove(turn)) status = check ? Status.CHECKMATE : Status.STALEMATE;
        else if (insufficientMaterial()) status = Status.DRAW_INSUFFICIENT;
        else if (halfmoveClock >= 100) status = Status.DRAW_FIFTY_MOVE;
        else status = check ? Status.CHECK : Status.ONGOING;
    }

    private boolean insufficientMaterial() {
        int minors = 0;
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = board.get(r, c);
                if (p == null || p.getType() == PieceType.KING) continue;
                if (p.getType() == PieceType.BISHOP || p.getType() == PieceType.KNIGHT) minors++;
                else return false;
            }
        return minors <= 1;
    }

    public String statusText() {
        String side = turn == Color.WHITE ? "White" : "Black";
        switch (status) {
            case CHECK: return side + " ki turn - CHECK!";
            case CHECKMATE: return "CHECKMATE! " + (turn == Color.WHITE ? "Black" : "White") + " jeet gaya";
            case STALEMATE: return "STALEMATE - Draw";
            case DRAW_INSUFFICIENT: return "Draw - insufficient material";
            case DRAW_FIFTY_MOVE: return "Draw - 50 move rule";
            default: return side + " ki turn";
        }
    }

    // ---------------- notation / PGN ----------------
    private String buildSan(int fr, int fc, int tr, int tc, Piece p, boolean capture, boolean castle, PieceType promo) {
        if (castle) return tc > fc ? "O-O" : "O-O-O";
        String dest = Board.name(tr, tc);
        StringBuilder sb = new StringBuilder();
        if (p.getType() == PieceType.PAWN) {
            if (capture) sb.append((char) ('a' + fc)).append('x');
            sb.append(dest);
            if (promo != null) sb.append('=').append(promo.getLetter());
            return sb.toString();
        }
        sb.append(p.getType().getLetter());
        boolean sameFile = false, sameRank = false, ambiguous = false;
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                if (r == fr && c == fc) continue;
                Piece o = board.get(r, c);
                if (o != null && o.getType() == p.getType() && o.getColor() == p.getColor()
                        && isLegalMove(r, c, tr, tc)) {
                    ambiguous = true;
                    if (c == fc) sameFile = true;
                    if (r == fr) sameRank = true;
                }
            }
        if (ambiguous) {
            if (!sameFile) sb.append((char) ('a' + fc));
            else if (!sameRank) sb.append(8 - fr);
            else sb.append((char) ('a' + fc)).append(8 - fr);
        }
        if (capture) sb.append('x');
        sb.append(dest);
        return sb.toString();
    }

    public String result() {
        if (status == Status.CHECKMATE) return turn == Color.WHITE ? "0-1" : "1-0";
        if (isGameOver()) return "1/2-1/2";
        return "*";
    }

    public String toPgn(String white, String black) {
        StringBuilder sb = new StringBuilder();
        sb.append("[Event \"CheckMate Java Game\"]\n");
        sb.append("[Site \"Local\"]\n");
        sb.append("[Date \"").append(LocalDate.now().toString().replace('-', '.')).append("\"]\n");
        sb.append("[White \"").append(white).append("\"]\n");
        sb.append("[Black \"").append(black).append("\"]\n");
        sb.append("[Result \"").append(result()).append("\"]\n\n");
        for (int i = 0; i < history.size(); i++) {
            if (i % 2 == 0) sb.append(i / 2 + 1).append(". ");
            sb.append(history.get(i).san).append(' ');
        }
        sb.append(result()).append('\n');
        return sb.toString();
    }

    public void savePgn(String path, String white, String black) throws IOException {
        Files.write(Paths.get(path), toPgn(white, black).getBytes("UTF-8"));
    }
}
