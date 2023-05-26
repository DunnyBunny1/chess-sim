package student.Pieces;

import student.*;

import java.util.*;

//Since Piece implements MoveGenerator so does King
public class King extends Piece {
    private boolean isInCheck;
    protected final static Offset[] candidateTargetSquares;
    public static final Tile whiteKingsideCastleTile;
    public static final Tile whiteQueensideCastleTile;
    public static final Tile blackKingsideCastleTile;
    public static final Tile blackQueensideCastleTile;
    public static final Map<Tile, Tile> castleTileToRookTile;

    public King(int rank, int file, boolean isWhite) {
        super(rank, file, isWhite);
    }

    static {
        candidateTargetSquares = new Offset[]{
                new Offset(0, -1),
                new Offset(0, +1),
                new Offset(-1, -1),
                new Offset(-1, 0),
                new Offset(-1, +1),
                new Offset(+1, +1),
                new Offset(+1, 0),
                new Offset(+1, -1)
        };
        whiteKingsideCastleTile = new Tile(7, 6);
        whiteQueensideCastleTile = new Tile(7, 2);
        blackKingsideCastleTile = new Tile(0, 6);
        blackQueensideCastleTile = new Tile(0, 2);
        castleTileToRookTile = new HashMap<>();
        castleTileToRookTile.put(whiteKingsideCastleTile, new Tile(7, 5));
        castleTileToRookTile.put(whiteQueensideCastleTile, new Tile(7, 3));
        castleTileToRookTile.put(blackKingsideCastleTile, new Tile(0, 5));
        castleTileToRookTile.put(blackQueensideCastleTile, new Tile(0, 3));
    }

    public boolean isInCheck() {
        return isInCheck;
    }

    public void setInCheck(boolean inCheck) {
        isInCheck = inCheck;
    }

    @Override
    public Set<Move> findLegalMoves(GameBoard board) {
        if (!this.movesCalculated) {
            Set<Tile> enemyTargetSquares = isWhite ? board.getBlackTargetSquares() : board.getWhiteTargetSquares();
            Tile[][] boardTiles = board.getBoardTiles();
            Tile startingTile = boardTiles[this.rank][this.file];

            for (Offset offset : candidateTargetSquares) {
                Tile ghostTile = new Tile(rank + offset.rankOffset(), file + offset.fileOffset());
                if (ghostTile.isInBounds()) {
                    Tile boardTile = boardTiles[ghostTile.getRank()][ghostTile.getRank()];
                    if (boardTile.isOccupied()) {
                        if (boardTile.getPiece().isWhite() != this.isWhite) {
                            legalMoves.add(new Move(startingTile, boardTile, true));
                        }
                    } else {
                        legalMoves.add(new Move(startingTile, boardTile, false));
                    }
                }
            }
            calculateCastlingMoves(board, startingTile);
            this.movesCalculated = true;
        }
        return this.legalMoves;
    }

    private void calculateCastlingMoves(GameBoard board, Tile startingTile) {
        Tile[][] boardTiles = board.getBoardTiles();
        if (isInCheck) {
            return;
        }
        if (isWhite) {
            if (board.whiteCanCastleKingside()) {
                if (hasSafeIntermediateTiles(board, true)) {
                    legalMoves.add(new Move(startingTile, true));
                }
            }
            if (board.whiteCanCastleQueenside()) {
                if (hasSafeIntermediateTiles(board, false)) {
                    legalMoves.add(new Move(startingTile, false));
                }
            }
        } else {
            if (board.blackCanCastleKingside()) {
                if (hasSafeIntermediateTiles(board, true)) {
                    legalMoves.add(new Move(startingTile, true));
                }
            }
            if (board.blackCanCastleQueenside()) {
                if (hasSafeIntermediateTiles(board, false)) {
                    legalMoves.add(new Move(startingTile, false));
                }
            }
        }

    }

    private boolean hasSafeIntermediateTiles(GameBoard board, boolean isKingSideCastle) {
        int rank = isWhite ? 7 : 0;
        boolean fileIncreasing = isKingSideCastle;
        int inc = fileIncreasing ? 1 : -1;
        Tile[][] boardTiles = board.getBoardTiles();
        Set<Tile> enemyTargetSquares = isWhite ? board.getBlackTargetSquares() : board.getWhiteTargetSquares();
        for (int currentFile = this.file; fileIncreasing ? currentFile < 7 : currentFile > 1; currentFile += inc) {
            Tile currentTile = boardTiles[rank][file];
            if (currentTile.isOccupied() || enemyTargetSquares.contains(currentTile)) {
                return false;
            }
        }
        return true;
    }

}
