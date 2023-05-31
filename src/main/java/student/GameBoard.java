package student;

import student.Pieces.*;

import java.awt.*;
import java.util.*;

//TODO: calculate all initial pins right away. For a board that is created from a fenstring of a board
// of a started game, we will need to calculate all pins. If the board is created from the starting fen
// string, we know that there are no pins since chess games have no pins in their initial positions.
// To calculate pins later in the game, on each move, we will check if it revealed a pin, took away a pin
// or created a pin. This way, we won't have to scan over every piece after every move to calculate pins
//TODO: Implement checks, pawn promotions, castling, fenstring extras, en passant,
// turn alternation, move logging (like e4)
//TODO: Implement game over conditions (draw, stalemate, checkmate, resignation)
//TODO: consider double checks (only king moves),
// the fact that knights and bishops can only check when they are on the same color as enemy king,
// rooks only have 2 checks (one along the rank, one along the column), only sliding piece checks can be blocked

public final class GameBoard {
    //TODO: make all vars final
    public static final String startingPosition;
    public static final int NUM_RANKS;
    public static final int NUM_FILES;
    public static final int NUM_PIXELS_PER_SQUARE;
    public static final int FRAME_WIDTH;
    public static final int FRAME_HEIGHT;
    public static final Theme theme; //TODO: add method to change theme with JMenuButton
    public static final Color LIGHT_SQUARE_COLOR;
    public static final Color DARK_SQUARE_COLOR;
    public static final int HASHING_PRIME_NUMBER;
    public static final Set<String> pawnPromotionPieceNames;
    public static final Set<Piece> pieceTypes;
    public static final Map<String, String> fenCharacterFromPieceType;

    private final Tile[][] boardTiles;
    private String fenString;
    private final Set<Tile> blackTargetSquares;
    private final Set<Tile> whiteTargetSquares;

    private String enPassantTargetSquare;
    private boolean whiteCanCastleKingside = false;
    private boolean whiteCanCastleQueenside = false;
    private boolean blackCanCastleKingside = false;
    private boolean blackCanCastleQueenside = false;
    private int halfMoveClock;
    private int fullMoveCounter;

    private boolean whiteToMove;

    //All chess boards must have at one black king and one white king
    private final King whiteKing;
    private final King blackKing;


    private Set<Piece> whitePieces;
    private Set<Piece> blackPieces;
    private Set<Piece> allPiecesOnBoard;


    static {
        startingPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        NUM_RANKS = 8;
        NUM_FILES = 8;
        NUM_PIXELS_PER_SQUARE = 64;
        FRAME_WIDTH = (NUM_PIXELS_PER_SQUARE * NUM_RANKS);
        FRAME_HEIGHT = (NUM_PIXELS_PER_SQUARE * NUM_FILES);
        theme = Theme.Ocean; //TODO: add method to change theme with JMenuButton
        LIGHT_SQUARE_COLOR = theme.lightSquareColor;
        DARK_SQUARE_COLOR = theme.darkSquareColor;
        HASHING_PRIME_NUMBER = 31;
        pawnPromotionPieceNames = new HashSet<>(Set.of("Knight", "Bishop", "Rook", "Queen"));

        fenCharacterFromPieceType = new HashMap<>();
        fenCharacterFromPieceType.put(King.class.getSimpleName(), "k");
        fenCharacterFromPieceType.put(Queen.class.getSimpleName(), "q");
        fenCharacterFromPieceType.put(Pawn.class.getSimpleName(), "p");
        fenCharacterFromPieceType.put(Knight.class.getSimpleName(), "n");
        fenCharacterFromPieceType.put(Bishop.class.getSimpleName(), "b");
        fenCharacterFromPieceType.put(Rook.class.getSimpleName(), "r");

        //These pieces all have negative ranks and files because their positions have not been set yet
        //We do not want them to appear on the board though, so they are all negative
        //even though these pieces are not on the board yet, they must cannot be on the same square, or else
        //they would be considered equals(). To circumvent this, each piece has a different file
        //Set.of returns an unmodifiable set
        pieceTypes = new HashSet<Piece>(Set.of(
                new Pawn(-1, -1, false),
                new Knight(-1, -1, false),
                new Bishop(-1, -1, false),
                new Queen(-1, -1, false),
                new Rook(-1, -1, false)
        ));
    }

    public GameBoard(String fenString) {
        this.fenString = fenString;
        this.whitePieces = new HashSet<>();
        this.blackPieces = new HashSet<>();
        this.allPiecesOnBoard = new HashSet<>();
        this.boardTiles = new Tile[8][8];
        this.whiteTargetSquares = new HashSet<>();
        this.blackTargetSquares = new HashSet<>();

        //In this step we are also checking if a fenstring is valid
        setPiecesAndTilesToNonStartingPosition(fenString);
        this.whiteKing = initializeKing(true);
        this.blackKing = initializeKing(false);
        calculateCheck(whiteToMove);
        //TODO: whatever's side turn it is, see if they are in check
    }


    private void setPiecesAndTilesToNonStartingPosition(String fenString) {
        Set<Piece> whitePieces = new HashSet<>();
        Set<Piece> blackPieces = new HashSet<>();
        try {
            String[] ranks = fenString.split("/");
            for (int rank = 0; rank < 8; rank++) {
                String currentRank = ranks[rank];
                char[] rankChars = currentRank.toCharArray();
                //the last segment of the fenString is extra long as it includes extras like the en passant target square,
                // castling privileges, and the half move counter.
                //Therefore, we need to do a special check on the last line of rankChars
                //The first space designates the end of the last rank
                if (rank == 7) {
                    currentRank = ranks[rank];
                    int endLastRankIndex = currentRank.indexOf(" ");
                    if (endLastRankIndex == -1) {
                        throw new IllegalArgumentException("Unable to parse last line of FEN String");
                    }
                    rankChars = currentRank.substring(0, endLastRankIndex).toCharArray();
                    String extras = currentRank.substring(endLastRankIndex);
                    String[] boardRights = extras.split(" ");
                    if (boardRights.length != 5) {
                        throw new IllegalArgumentException("Too many or too few boardRights passed in " +
                                "last line of FEN String");
                    }
                    for (int i = 0; i < 5; i++) {
                        String currentRight = boardRights[i];
                        switch (i) {
                            case 0 -> { // the first board right is side to move
                                switch (currentRight) {
                                    case "w" -> this.whiteToMove = true;
                                    case "b" -> this.whiteToMove = false;
                                    default ->
                                            throw new IllegalArgumentException("Illegal Fenstring: invalid side to move");
                                }
                            }
                            case 1 -> { //the second board right is castling rights
                                for (char castlingRight : currentRight.toCharArray()) {
                                    if (castlingRight == '-') {
                                        break;
                                    }
                                    switch (castlingRight) {
                                        case 'K' -> this.whiteCanCastleKingside = true;
                                        case 'k' -> this.blackCanCastleKingside = true;
                                        case 'Q' -> this.whiteCanCastleQueenside = true;
                                        case 'q' -> this.blackCanCastleQueenside = true;
                                        default ->
                                                throw new IllegalArgumentException("Illegal Fenstring: invalid castling rights");
                                    }
                                }
                            }
                            case 2 -> {//the third board right is en passant target square
                                if (currentRight.equals("-")) {
                                    this.enPassantTargetSquare = "";
                                } else {
                                    String[] letterSquareCombo = currentRight.split("");
                                    if (letterSquareCombo.length == 2) {
                                        String fileLetter = letterSquareCombo[0];
                                        String rankLetterString = letterSquareCombo[1];
                                        int rankLetter = -1;
                                        if (isParsable(rankLetterString)) {
                                            rankLetter = Integer.parseInt(rankLetterString);
                                        } else {
                                            throw new IllegalArgumentException("Illegal fenstring: error in en passant target square");
                                        }
                                        if ("abcdefgh".contains(fileLetter) && rankLetter >= 0 && rankLetter <= 8) {
                                            this.enPassantTargetSquare = currentRight;
                                        } else {
                                            throw new IllegalArgumentException("Illegal fenstring: error in en passant target square clock");
                                        }
                                    }
                                    this.enPassantTargetSquare = currentRight;
                                }
                            }
                            case 3 -> {//the fourth board right is the halfmove clock
                                if (isParsable(currentRight)) {
                                    this.halfMoveClock = Integer.parseInt(currentRight);
                                } else {
                                    throw new IllegalArgumentException("Illegal fenstring: error in halmove clock");
                                }
                            }
                            case 4 -> {//the fifth and final board right is the full move clock
                                if (isParsable(currentRight)) {
                                    this.fullMoveCounter = Integer.parseInt(currentRight);
                                } else {
                                    throw new IllegalArgumentException("Illegal fenstring: error in halmove clock");
                                }
                            }
                            //our index variable "i"
                        }
                    }
                    //TODO: use extras. extras contains castling privileges, en passant target square, etc.

                }
                int file = 0;
                for (int i = 0; i < rankChars.length; i++) {
                    String currentPosition = String.valueOf(rankChars[i]);
                    //current position can be a piece or a number of empty tiles
                    //if it is parsable (if it is a number), we want to add that number  of empty tiles
                    if (isParsable(currentPosition)) {
                        int numEmptySpaces = Integer.parseInt(currentPosition);
                        for (int j = file; j < 8 && j < numEmptySpaces; j++) {
                            boardTiles[rank][j] = new Tile(rank, j);
                            file++;
                        }
                        continue;
                    }
                    String lowerCasePosition = currentPosition.toLowerCase();
                    //capital letters correspond to white pieces. If the piece String is lowercase, then
                    //it should be black. Otherwise, it should be white
                    boolean isWhite = !currentPosition.equals(lowerCasePosition);
                    Piece newPiece =
                            switch (lowerCasePosition) {
                                case "k" -> new King(rank, file, isWhite);
                                case "q" -> new Queen(rank, file, isWhite);
                                case "p" -> new Pawn(rank, file, isWhite);
                                case "n" -> new Knight(rank, file, isWhite);
                                case "b" -> new Bishop(rank, file, isWhite);
                                case "r" -> new Rook(rank, file, isWhite);
                                default -> null;
                            };

                    if (Objects.equals(null, newPiece)) {
                        throw new IllegalArgumentException("FEN String contained illegal character. " +
                                "Legal characters are digits 0-8, squares (a-h, 1-8) (for example e4)," +
                                " and capital or lowercase k,q,p,n,b,r");
                    }
                    Tile newTile = new Tile(rank, file, newPiece);
                    boardTiles[rank][file] = newTile;
                    newPiece.tile = newTile;
                    if (isWhite) {
                        whitePieces.add(newPiece);
                    } else {
                        blackPieces.add(newPiece);
                    }
                    file++;
                }
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null) {
                System.out.printf("Message: %s ", message);
            }
            System.err.printf("Unable to generate fenString from %s", fenString);
        }
        this.whitePieces = whitePieces;
        this.blackPieces = blackPieces;
        this.allPiecesOnBoard.addAll(whitePieces);
        this.allPiecesOnBoard.addAll(blackPieces);
    }

    private King initializeKing(boolean isWhite) {
        int colorKingCount = 0;
        King king = new King(-1, -1, false);
        for (Piece piece : this.allPiecesOnBoard) {
            if (piece instanceof King newKing) {
                if (piece.isWhite == isWhite) {
                    king = newKing;
                    colorKingCount++;
                }
            }
        }
        if (king.rank == -1 || colorKingCount != 1) {
            throw new IllegalArgumentException("Illegal fenstring passed: " +
                    "it contained 0 or 2+ kings of the same color." +
                    "Pieces passed: " + this.allPiecesOnBoard);
        }
        return king;
    }

    //static GameBoard methods
    public static String getLetterSquareCombination(int rank, int file) {
        String letters = "abcdefgh";
        return letters.substring(file, file + 1) + (8 - rank);
    }

    private static boolean isParsable(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }


    //getter methods
    public Tile[][] getBoardTiles() {
        return this.boardTiles;
    }

    public Set<Tile> getBlackTargetSquares() {
        return blackTargetSquares;
    }

    public Set<Tile> getWhiteTargetSquares() {
        return whiteTargetSquares;
    }

    public King getWhiteKing() {
        return whiteKing;
    }

    public King getBlackKing() {
        return blackKing;
    }

    public String getEnPassantTargetSquare() {
        return this.enPassantTargetSquare;
    }

    public boolean whiteCanCastleKingside() {
        return this.whiteCanCastleKingside;
    }

    public boolean whiteCanCastleQueenside() {
        return this.whiteCanCastleQueenside;
    }

    public boolean blackCanCastleKingside() {
        return this.blackCanCastleKingside;
    }

    public boolean blackCanCastleQueenside() {
        return this.blackCanCastleQueenside;
    }


    @Override
    public String toString() {
        return this.fenString;
    }

    //returns the FEN (Forsynth-Edwards Notation) String represenation of the board
    //FEN Strings go from top to bottom, left to right
    // "/" represents a new rank
    // a number represent an empty square on a given rank
    // For each rank, starting going top to bottom, from 0-7 (technically from 8-1)
    // For each file in that row going left to right, from 0-7 (technically from 1-8)
    // numbers 1-8 re
    private String buildFenString() {
        StringBuilder fenBuilder = new StringBuilder();
        for (int rank = 0; rank < NUM_RANKS; rank++) {
            int emptyTileCount = 0;
            for (int file = 0; file < NUM_FILES; file++) {
                Tile currentTile = boardTiles[rank][file];
                if (!(currentTile.isOccupied())) {
                    emptyTileCount++;
                    continue;
                }
                if (emptyTileCount > 0) {
                    fenBuilder.append(emptyTileCount);
                    emptyTileCount = 0;
                }
                Piece currentPiece = currentTile.getPiece();
                boolean isWhite = currentPiece.isWhite();
                String pieceType = currentPiece.getClass().getSimpleName();
                String pieceString = fenCharacterFromPieceType.get(pieceType);
                fenBuilder.append(isWhite ? (pieceString.toUpperCase()) : (pieceString));
            }
            if (emptyTileCount > 0) {
                fenBuilder.append(emptyTileCount);
            }
            fenBuilder.append('/');
        }
        //TODO: add fenstring extras here like castling privileges, en passant target, etc.

        return fenBuilder.toString();
    }

    /**
     * Makes an actual move on this board, mutating its instance fields and continues the game.
     * Happens after a player chooses a move or the engine chooses a move
     * This can only be called on the single GameBoard instance in BoardFrame, the actual game.
     *
     * @param move
     */
    public void makeActualMove(Move move) {
        if (move == null) {
            throw new RuntimeException("Null move passed to GameBoard.makeMove()");
        }
        //store the current board instance old fenstring
        //apply the move to the current board instance
        if (move.isCastlingMove()) {
            makeCastleMove(move);
        } else if (move.isPawnPromotion()) {
            makePawnPromotionMove(move);
        } else if (move.isEnPassantMove()) {
            makeEnPassantMove(move);
        } else {
            Piece piece = move.getStartingTile().getPiece();
            moveToTile(piece, move.getTargetTile());
        }
        for (Piece piece : this.allPiecesOnBoard) {
            piece.targetTiles.clear();
            piece.movesCalculated = false;
        }
        this.blackTargetSquares.clear();
        this.whiteTargetSquares.clear();
        this.fenString = buildFenString();
    }

    /**
     * Makes a potential move on this board in a Game Tree, returning the child board and not affecting
     * the current board. After the move is made, it is unapplied, and the instnace fields are restored.
     *
     * @param move
     * @return The child gameboard as if the move were made on the original board
     */
    //TODO: update the boardTiles here. Unless they update automatically?
    public GameBoard getChildBoard(Move move) {
        if (move == null) {
            throw new RuntimeException("Null move passed to GameBoard.makeMove()");
        }
        //store the current board instance old fenstring
        String oldFenstring = this.fenString;
        assert (this.fenString.equals(buildFenString()));
        //apply the move to the current board instance
        Optional<Piece> optionalCapturedPiece = Optional.empty();
        if (move.isCastlingMove()) {
            makeCastleMove(move);
        } else if (move.isPawnPromotion()) {
            optionalCapturedPiece = makePawnPromotionMove(move);
        } else if (move.isEnPassantMove()) {
            optionalCapturedPiece = makeEnPassantMove(move);
        } else {
            Piece piece = move.getStartingTile().getPiece();
            optionalCapturedPiece = moveToTile(piece, move.getTargetTile());
        }

        //recalculate the current board fenstring after the move
        String updatedFenString = this.buildFenString();
        assert (!(this.fenString.equals(updatedFenString)));

        //unapply the move to the current game board instance
        if (optionalCapturedPiece.isPresent()) {
            this.unapplyMove(move, optionalCapturedPiece.get());
        } else {
            this.unapplyMove(move, null);
        }

        assert (this.fenString.equals(buildFenString()));

        //create a new GameBoard instance with the updated fenString
        //update the whiteToMove flag
        return new GameBoard(updatedFenString);
    }

    public Optional<Piece> moveToTile(Piece piece, Tile newTile) {

        if (piece.tile != null) {
            piece.tile.putPiece(null); //updates the piece property of the starting tile
        }
        Piece capturedPiece = null;
        if (newTile.isOccupied()) {
            capturedPiece = newTile.getPiece();
            if (capturedPiece.isWhite) {
                if (!(this.whitePieces.remove(capturedPiece) && this.allPiecesOnBoard.remove(capturedPiece))) {
                    throw new IllegalArgumentException(String.format
                            ("Error occured when capturing piece %s with %s", capturedPiece, piece));
                }
            } else {
                if (!(this.blackPieces.remove(capturedPiece) && this.allPiecesOnBoard.remove(capturedPiece))) {
                    throw new IllegalArgumentException(String.format
                            ("Error occured when capturing piece %s with %s", capturedPiece, piece));
                }
            }
        }
        piece.tile = newTile; //sets the tile property of this piece to the new tile
        newTile.putPiece(piece); //sets the piece property of this piece's tile to this piece
        piece.rank = newTile.getRank();
        piece.file = newTile.getFile();
        return Optional.ofNullable(capturedPiece);
    }

    private void unapplyMove(Move move, Piece capturedPiece) {
        if (move == null) {
            throw new IllegalArgumentException("Unable to unapply null move");
        }
        if (capturedPiece == null) {

        } else {

        }
        //TODO: implement
    }

    //in the case of castling, the victim tile refers to the designated rook square
    private void makeCastleMove(Move move) {
        //TODO: implement


    }

    private Optional<Piece> makePawnPromotionMove(Move move) {
        //TODO: implement
        return Optional.ofNullable(null);
    }

    private Optional<Piece> makeEnPassantMove(Move move) {
        //TODO: implement
        return Optional.of(new Pawn(-1, -1, true));
    }

    //TODO: do something about the captured piece (like removing it and setting its
    //TODO:board tile to an empty board tile and updating the fenString
    private void makeCaptureMove(Move move) {
        if (!(move.isCapture())) {
            throw new IllegalArgumentException("makeCaptureMove() cannot be called with non capture move.");
        }
        Tile startingTile = move.getStartingTile();
        Tile targetTile = move.getTargetTile();
        Piece attacker = startingTile.getPiece();
        Piece victim = targetTile.getPiece();
        if (attacker.isWhite != victim.isWhite) {
            throw new IllegalArgumentException(
                    String.format("Piece %s at position %s  is unable to capture Piece %s at position %s" +
                                    "because they are the same color",
                            attacker, getLetterSquareCombination(attacker.rank, attacker.file), victim,
                            getLetterSquareCombination(victim.rank, victim.file)));
        }
        moveToTile(attacker, targetTile);
    }

    private void calculateCheck(boolean whiteToMove) {
        if (whiteToMove) {
            //if we're calculating checks for white, that means black just moved.
            //Let's see if any black pieces are looking at the whiteKing
            boolean inCheck = blackTargetSquares.contains(whiteKing.tile);
            whiteKing.setInCheck(inCheck);
        } else {
            boolean inCheck = whiteTargetSquares.contains(blackKing.tile);
            blackKing.setInCheck(inCheck);
        }
    }
}
