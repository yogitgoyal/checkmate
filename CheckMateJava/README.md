# CheckMate Java - OOP Chess Engine + Swing GUI

2-player chess: full legal move rules, check/checkmate/stalemate, castling, en passant,
pawn promotion, move log, PGN export.

## Run
Requirements: JDK 17+ (`javac -version` se check karo)

    javac -d out $(find src -name "*.java")      # Windows: run.bat use karo
    java -cp out gui.Main                        # game
    java -cp out test.EngineTests                # tests

## Structure
- model/  : Color, PieceType, Piece (abstract), Rook/Bishop/Queen/Knight/King/Pawn, Board
- engine/ : GameEngine (turn, legal moves, check, status, PGN), Move
- gui/    : ChessGUI (Swing), Main
- test/   : EngineTests (Fool's Mate, Scholar's Mate, stalemate, pin, castling, en passant, promotion)

## OOP concepts
Abstraction (Piece), Inheritance (6 pieces), Polymorphism (isValidMove/attacks override),
Encapsulation (private board state, getters), Enums, Separation of concerns (model/engine/gui).

## Not included (future scope)
Threefold repetition, undo, AI (minimax + alpha-beta), network play.
