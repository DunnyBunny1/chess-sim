package student.Pieces;

import student.*;

import java.util.HashSet;
import java.util.Set;

public class Knight extends Piece {
    protected static Offset[] candidateTargetSquares;

    public Knight(int rank, int file, boolean isWhite) {
        super(rank, file, isWhite);
    }


    static {
        candidateTargetSquares = new Offset[]{
                new Offset(1, 2),
                new Offset(1, -2),
                new Offset(-1, 2),
                new Offset(-1, -2),
                new Offset(-2, -1),
                new Offset(-2, 1),
                new Offset(2, -1),
                new Offset(2, 1)
        };
    }


    @Override
    public Set<Move> findLegalMoves(GameBoard board) {
        Tile[][] boardTiles = board.getBoardTiles();
        Tile thisTile = boardTiles[this.rank][this.file];
        //unpinned knights are free to move wherever they want and do not have to worry about being blocked
        //Like all pieces, knights can move to any square that
        //unpinned knights can move to any square that is unoccupied or occupied by an empty piece
        for (Offset offset : candidateTargetSquares) {
            Tile ghostTile = new Tile(rank + offset.rankOffset(), file + offset.fileOffset());
            if (ghostTile.isInBounds()) {
                Tile boardTile = boardTiles[ghostTile.getRank()][ghostTile.getFile()];
                if (boardTile.isOccupied()) {
                    //if a piece is of a different color we can capture it
                    if (boardTile.getPiece().isWhite() != this.isWhite()) {
                        Move knightCapture = (new Move(thisTile, boardTile, true));
                        addPseudolegalMove(board, knightCapture);
                    }
                    //if the square is unoccupied we can move there no problem
                } else {
                    Move nonCapture = new Move(thisTile, boardTile, false);
                    addPseudolegalMove(board, nonCapture);
                }
            }
        }
        return legalMoves;
    }
}
