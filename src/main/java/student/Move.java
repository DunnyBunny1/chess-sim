package student;

import student.Pieces.King;

import java.util.Objects;

//all records are final by default
public class Move {
    private Tile startingTile;
    private Tile targetTile;
    private boolean isCapture;
    private boolean isEnPassantMove;
    private String pawnPromotionPieceName;
    private Tile victimTile;
    private boolean isKingSideCastle;
    private boolean isCastlingMove;


    //regular move
    public Move(Tile startingTile, Tile targetTile, boolean isCapture) {
        this(startingTile, targetTile, isCapture, "none");
    }

    //castling move
    public Move(Tile startingTile, boolean isKingSideCastle) {
        this.startingTile = startingTile;
        Piece castlingPiece = startingTile.getPiece();
        if (!(castlingPiece instanceof King)) {
            throw new IllegalArgumentException("Cannot make a castling move with piece" + castlingPiece);
        }
        this.isCastlingMove = true;
        this.isKingSideCastle = isKingSideCastle;
        if (castlingPiece.isWhite) {
            if (isKingSideCastle) {
                this.targetTile = King.whiteKingsideCastleTile;
            } else {
                this.targetTile = King.whiteQueensideCastleTile;
            }
        } else {
            if (isKingSideCastle) {
                this.targetTile = King.blackKingsideCastleTile;
            } else {
                this.targetTile = King.blackQueensideCastleTile;
            }
        }
        this.victimTile = King.castleTileToRookTile.get(targetTile);
        this.pawnPromotionPieceName = "none";
        updatePieceTargetTiles();
    }


    //pawn promotion move
    public Move(Tile startingTile, Tile targetTile, boolean isCapture, String pawnPromotionPieceName) {
        this.startingTile = startingTile;
        this.targetTile = targetTile;
        this.victimTile = targetTile;
        this.isCapture = isCapture;
        this.isEnPassantMove = false;
        this.isCastlingMove = false;
        this.pawnPromotionPieceName = pawnPromotionPieceName;
        updatePieceTargetTiles();

    }

    //en passant move
    public Move(Tile startingTile, Tile enPassantTargetSquare) {
        this.startingTile = startingTile;
        this.targetTile = enPassantTargetSquare;
        this.victimTile = enPassantTargetSquare;
        this.isCapture = true;
        this.isEnPassantMove = true;
        this.pawnPromotionPieceName = "none";
        this.isCastlingMove = false;
        updatePieceTargetTiles();
    }

    private void updatePieceTargetTiles() {
        Piece piece = startingTile.getPiece();
        if (piece == null) {
            throw new IllegalArgumentException("Null piece passed for move");
        }
        if ((!(targetTile.isOccupied())) || (targetTile.isOccupied() && targetTile.getPiece().isWhite != piece.isWhite)) {
            piece.targetTiles.add(targetTile);
        } else {
            throw new IllegalArgumentException(String.format("Illegal target tile passed for move %s", this));
        }
    }

    public Tile getStartingTile() {
        return startingTile;
    }

    public Tile getTargetTile() {
        return targetTile;
    }

    public Tile getVictimTile() {
        return this.victimTile;
    }

    public boolean isCapture() {
        return isCapture;
    }

    public boolean isCastlingMove() {
        return this.isCastlingMove;
    }

    public boolean isEnPassantMove() {
        return this.isEnPassantMove;
    }

    public boolean isPawnPromotion() {
        return !(Objects.equals(this.pawnPromotionPieceName, "none"));
    }

    public String getPawnPromotionPieceName() {
        if (this.isPawnPromotion()) {
            return this.pawnPromotionPieceName;
        }
        throw new RuntimeException("Tried to access pawn promotion piece on non pawn promotion move" + this);
    }

    @Override
    public String toString() {
        //TODO: finish and add functionality for when two pieces can make the move, for example rfxe4/
        // return attacker.GameBoard.getLetterSquareCombination(targetTile)
        return startingTile.getPiece() + (isCapture ? "takes" + targetTile.getPiece() : "to" + targetTile);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Move otherMove) {
            return otherMove.startingTile.equals(this.startingTile) &&
                    otherMove.targetTile.equals(this.targetTile) &&
                    Objects.equals(this.pawnPromotionPieceName, otherMove.pawnPromotionPieceName) &&
                    this.victimTile == otherMove.victimTile;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashingPrimeNumber = GameBoard.HASHING_PRIME_NUMBER;
        int hash = 1;
        hash = hash * hashingPrimeNumber + startingTile.hashCode();
        hash = hash * hashingPrimeNumber + targetTile.hashCode();
        hash = hash * hashingPrimeNumber + victimTile.hashCode();
        hash = hash * hashingPrimeNumber + pawnPromotionPieceName.hashCode();
        return hash;
    }

}
