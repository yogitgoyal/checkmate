package gui;

import engine.GameEngine;
import engine.Move;
import model.Board;
import model.Color;
import model.Piece;
import model.PieceType;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** UI layer: sirf click lena aur board dikhana. Saari logic GameEngine mein hai. */
public class ChessGUI extends JFrame {

    private static final java.awt.Color LIGHT = new java.awt.Color(240, 217, 181);
    private static final java.awt.Color DARK = new java.awt.Color(181, 136, 99);
    private static final java.awt.Color SELECTED = new java.awt.Color(246, 246, 105);
    private static final java.awt.Color MOVE_HINT = new java.awt.Color(130, 200, 120);
    private static final java.awt.Color CAPTURE_HINT = new java.awt.Color(235, 110, 100);

    private GameEngine engine = new GameEngine();
    private final JButton[][] squares = new JButton[8][8];
    private int selR = -1, selC = -1;
    private List<int[]> targets = new ArrayList<>();
    private final JLabel statusLabel = new JLabel();
    private final JTextArea logArea = new JTextArea(20, 16);

    public ChessGUI() {
        super("CheckMate Java");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                JButton b = new JButton();
                b.setFont(new Font("Dialog", Font.PLAIN, 42));
                b.setPreferredSize(new Dimension(72, 72));
                b.setFocusPainted(false);
                b.setOpaque(true);
                b.setBorderPainted(false);
                b.setMargin(new java.awt.Insets(0, 0, 0, 0));
                final int row = r, col = c;
                b.addActionListener(e -> onSquareClicked(row, col));
                squares[r][c] = b;
                boardPanel.add(b);
            }
        }

        statusLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JButton newGame = new JButton("New Game");
        newGame.addActionListener(e -> {
            engine = new GameEngine();
            clearSelection();
            refresh();
        });
        JButton savePgn = new JButton("Save PGN");
        savePgn.addActionListener(e -> savePgn());

        JPanel buttons = new JPanel(new GridLayout(1, 2, 6, 6));
        buttons.add(newGame);
        buttons.add(savePgn);

        JPanel side = new JPanel(new BorderLayout(4, 4));
        side.add(new JLabel("Move log"), BorderLayout.NORTH);
        side.add(new JScrollPane(logArea), BorderLayout.CENTER);
        side.add(buttons, BorderLayout.SOUTH);
        side.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 8));

        add(statusLabel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(side, BorderLayout.EAST);

        refresh();
        pack();
        setLocationRelativeTo(null);
    }

    private void onSquareClicked(int r, int c) {
        if (engine.isGameOver()) return;
        Piece clicked = engine.getBoard().get(r, c);

        if (selR >= 0 && containsTarget(r, c)) {
            Piece moving = engine.getBoard().get(selR, selC);
            PieceType promo = PieceType.QUEEN;
            if (moving.getType() == PieceType.PAWN && (r == 0 || r == 7)) promo = askPromotion();
            engine.makeMove(selR, selC, r, c, promo);
            clearSelection();
            refresh();
            if (engine.isGameOver()) {
                JOptionPane.showMessageDialog(this, engine.statusText(), "Game Over", JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        }

        if (clicked != null && clicked.getColor() == engine.getTurn()) {
            selR = r;
            selC = c;
            targets = engine.legalMovesFrom(r, c);
        } else {
            clearSelection();
        }
        refresh();
    }

    private boolean containsTarget(int r, int c) {
        for (int[] t : targets) if (t[0] == r && t[1] == c) return true;
        return false;
    }

    private void clearSelection() {
        selR = -1;
        selC = -1;
        targets = new ArrayList<>();
    }

    private PieceType askPromotion() {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        PieceType[] types = {PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT};
        int choice = JOptionPane.showOptionDialog(this, "Pawn ko kya banana hai?", "Promotion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        return choice < 0 ? PieceType.QUEEN : types[choice];
    }

    private void refresh() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                JButton b = squares[r][c];
                Piece p = engine.getBoard().get(r, c);
                b.setText(p == null ? "" : p.getSymbol());
                b.setForeground(java.awt.Color.BLACK);
                java.awt.Color bg = (r + c) % 2 == 0 ? LIGHT : DARK;
                if (r == selR && c == selC) bg = SELECTED;
                else if (containsTarget(r, c)) bg = (p != null) ? CAPTURE_HINT : MOVE_HINT;
                b.setBackground(bg);
            }
        }
        statusLabel.setText(engine.statusText());

        StringBuilder sb = new StringBuilder();
        List<Move> h = engine.getHistory();
        for (int i = 0; i < h.size(); i++) {
            if (i % 2 == 0) sb.append(i / 2 + 1).append(". ");
            sb.append(h.get(i).san).append(i % 2 == 0 ? "  " : "\n");
        }
        logArea.setText(sb.toString());
    }

    private void savePgn() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("game.pgn"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                engine.savePgn(fc.getSelectedFile().getPath(), "Player 1", "Player 2");
                JOptionPane.showMessageDialog(this, "PGN save ho gaya!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Save fail: " + ex.getMessage());
            }
        }
    }
}
