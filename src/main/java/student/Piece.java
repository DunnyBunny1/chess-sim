package student;


import java.util.HashSet;
import java.util.Set;

public abstract class Piece implements MoveGenerator {
    protected int rank;
    protected int file;
    protected String pieceId = "";
    protected final boolean isWhite;
    protected Tile tile;
    protected Set<Tile> targetTiles;
    protected Set<Move> legalMoves;
    protected static final Set<String> pieceTypes;
    protected boolean movesCalculated;

    static {
        pieceTypes = new HashSet<>(Set.of(
                "Bishop", "King", "Knight", "Pawn", "Queen", "Rook"
        ));
    }

    protected Piece(int rank, int file, boolean isWhite) {
        this.rank = rank;
        this.file = file;
        this.isWhite = isWhite;
        this.legalMoves = new HashSet<>();
        this.targetTiles = new HashSet<>();
        this.movesCalculated = false;
    }

    public final String getPieceId() {
        if (pieceId.equals("")) {
            pieceId = (isWhite ? "white" : "black") + this.getClass().getSimpleName();
        }
        return pieceId;
    }

    protected final void addPseudolegalMove(GameBoard board, Move move) {
        GameBoard childBoard = board.getChildBoard(move);
        //if the move does not leave us in check, we can make it
        if (!(isWhite ? childBoard.getWhiteKing().isInCheck() : childBoard.getBlackKing().isInCheck())) {
            legalMoves.add(move);
        }
    }

    //TODO: move this method to gameboard


    public final boolean isWhite() {
        return this.isWhite;
    }

    public final int getRank() {
        return rank;
    }

    public final int getFile() {
        return file;
    }

    public final Set<Tile> getTargetTiles(GameBoard board) {
        if (!movesCalculated) {
            this.findLegalMoves(board);
            movesCalculated = true;
        }
        if (isWhite) {
            board.getWhiteTargetSquares().addAll(this.targetTiles);
        } else {
            board.getBlackTargetSquares().addAll(this.targetTiles);
        }
        return this.targetTiles;
    }

    @Override
    public final String toString() {
        //Returns "Rook at position e4" for a rook on e4, "Pawn at position h7" for a pawn on h7, etc.
        //if we do not override, we get a memory location or student.Pieces.bishop instead of Bishop
        return (this.isWhite ? "White " : "Black ") + this.getClass().getSimpleName() +
                " at position " + ((rank < 0 || rank > 7 || file < 0 || file > 7) ? ("out of bounds") :
                rank + ", " + file) + ".";
    }

    @Override
    public final boolean equals(Object other) {
        if (other instanceof Piece otherPiece) {
            return this.getClass() == otherPiece.getClass()
                    && this.isWhite == otherPiece.isWhite
                    && this.rank == otherPiece.rank && this.file == otherPiece.file;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        int hashingPrimeNumber = GameBoard.HASHING_PRIME_NUMBER;
        int hash = 1;
        hash = hash * hashingPrimeNumber + this.toString().hashCode();
        hash = hash * hashingPrimeNumber + rank;
        hash = hash * hashingPrimeNumber + file;
        return hash;
    }


}

