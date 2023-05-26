package student;

import java.util.HashSet;
import java.util.Set;

public interface SlidingMoveGenerator extends MoveGenerator {

    boolean canMoveVertically();

    boolean canMoveHorizontally();

    boolean canMoveDiagonally();

    default Set<Move> calculateDirectionalMoves(GameBoard board, Direction direction, Piece piece) {
        int rank = piece.rank;
        int file = piece.file;
        Tile pieceTile = piece.tile;
        Set<Move> legalMoves = new HashSet<>();
        switch (direction) {
            case DIAGONAL -> {
                //Each piece has one diagonal up and to the right, one up and to the left, one down and to the right, and
                //one down and to the left

                //finds diagonal moves left and above our piece on the board
                //conditions: rank is decreasing and file is decreasing
                findDiagonalSegmentMoves(pieceTile, board, rank, false, false);

                //finds diagonals moves right and above our piece on the board
                //conditions: rank is decreasing and file is increasing
                findDiagonalSegmentMoves(pieceTile, board, rank, true, false);

                //finds diagonals moves right and above our piece on the board
                //conditions: rank is increasing and file is decreasing
                findDiagonalSegmentMoves(pieceTile, board, rank, false, true);

                //finds diagonals moves right and above our piece on the board
                //conditions: rank is increasing and file is increasing
                findDiagonalSegmentMoves(pieceTile, board, rank, true, true);
            }

            //when calculating horizontal moves, the rank is constant, and we need to check each file in our rank
            case HORIZONTAL -> {
                //finds horizontal moves to the right of our piece
                findStraightLineMoves(
                        pieceTile, board, file + 1, 7, true, true
                );
                //finds horizontal moves to the left of our piece
                findStraightLineMoves(
                        pieceTile, board, file - 1, 0, true, false
                );
            }


            //when calculating vertical moves, the file is constant, and we need to check each rank in our file
            case VERTICAL -> {
                //finds vertical moves above our piece
                findStraightLineMoves(
                        pieceTile, board, rank - 1, 0, false, false
                );
                //finds vertical moves below our piece
                findStraightLineMoves(
                        pieceTile, board, rank + 1, 7, false, true
                );
            }
        }
        return legalMoves;
    }

    //all straight lines will either have a constant rank or constant file. If the constant dimension is the rank,
    //constant rank will be true. Otherwise, the constant dimension is the file and constantRank is false
    default void findStraightLineMoves(Tile pieceTile, GameBoard board, int startIndex,
                                       int endIndex, boolean constantRank, boolean dimensionIncreasing) {
        Piece piece = pieceTile.getPiece();
        int rank = piece.rank;
        int file = piece.file;
        Tile[][] boardTiles = board.getBoardTiles();
        int increment = dimensionIncreasing ? 1 : -1;
        for (int square = startIndex; dimensionIncreasing ? square <= endIndex : square >= endIndex; square += increment) {
            Tile currentTile = constantRank ? boardTiles[rank][square] : boardTiles[square][file];
            if (currentTile.isOccupied()) {
                if (currentTile.getPiece().isWhite() != piece.isWhite()) {
                    Move capture = new Move(pieceTile, currentTile, true);
                    piece.addPseudolegalMove(board, capture);

                }
                //since sliding pieces cannot jump over pieces, once there is a piece in the given direction,
                //we know that we will be unable to move any further in the direction, so we can break out of
                //our loop
                break;
            } else {
                Move nonCapture = new Move(pieceTile, currentTile, false);
                piece.addPseudolegalMove(board, nonCapture);
            }
        }
    }

    default void findDiagonalSegmentMoves(Tile pieceTile, GameBoard board, int pieceRank, boolean fileIncreasing, boolean rankIncreasing) {
        Piece piece = pieceTile.getPiece();
        int file = pieceRank;
        int startRank = rankIncreasing ? pieceRank + 1 : pieceRank - 1;
        int rankIncrement = rankIncreasing ? 1 : -1;
        int fileIncrement = fileIncreasing ? 1 : -1;
        Tile[][] boardTiles = board.getBoardTiles();
        for (int rank = startRank; rankIncreasing ? rank < 7 : rank > 0; rank += rankIncrement) {

            if (file < 0 || file > 7) {
                break;
            }
            Tile currentTile = boardTiles[rank][file];
            if (currentTile.isOccupied()) {
                if (currentTile.getPiece().isWhite() != piece.isWhite()) {
                    Move capture = new Move(pieceTile, currentTile, true);
                    piece.addPseudolegalMove(board, capture);

                }
                //since sliding pieces cannot jump over pieces, once there is a piece in the given direction,
                //we know that we will be unable to move any further in the direction, so we can break out of
                //our loop
                break;
            } else {
                Move nonCapture = new Move(pieceTile, currentTile, false);
                piece.addPseudolegalMove(board, nonCapture);
            }
            rank += rankIncrement;
            file += fileIncrement;
        }
    }

    default Set<Move> findSlidingMoves(GameBoard board, Piece piece) {
        if (canMoveDiagonally()) {
            calculateDirectionalMoves(board, Direction.DIAGONAL, piece);
        }
        if (canMoveHorizontally()) {
            calculateDirectionalMoves(board, Direction.HORIZONTAL, piece);
        }
        if (canMoveVertically()) {
            calculateDirectionalMoves(board, Direction.VERTICAL, piece);
        }
        return piece.legalMoves;
    }
}



