package student.Pieces;

import student.*;

import java.util.*;

public class Pawn extends Piece {
    protected final List<Tile> candidateTargetSquares = new ArrayList<>();
    final int dimensionOffset;
    Tile upForward; //square one forward
    Tile upLeft;//square up left
    Tile upRight; //square up right
    Tile twoForward; //square two forward

    public Pawn(int rank, int file, boolean isWhite) {
        super(rank, file, isWhite);
        this.dimensionOffset = isWhite ? 1 : -1;

    }

    private void recalculateCandidateTargetSquares() {
        this.upForward = new Tile(rank - dimensionOffset, file);
        this.upLeft = new Tile(rank - dimensionOffset, file - dimensionOffset);
        this.upRight = new Tile(rank - dimensionOffset, file + dimensionOffset);
        this.twoForward = new Tile(rank - (2 * dimensionOffset), file);
        this.candidateTargetSquares.add(upForward);
        this.candidateTargetSquares.add(upLeft);
        this.candidateTargetSquares.add(upRight);
        this.candidateTargetSquares.add(twoForward);

    }

    @Override
    public Set<Move> findLegalMoves(GameBoard board) {
        recalculateCandidateTargetSquares();
        Tile[][] boardTiles = board.getBoardTiles();
        Tile thisTile = boardTiles[this.rank][this.file];
        King friendlyKing = isWhite ? board.getWhiteKing() : board.getBlackKing();
        int kingRank = friendlyKing.getRank();
        int kingFile = friendlyKing.getFile();
        //Pawns are the only piece whose movement is limited by their color
        //Black pawns can move down the board (move to squares of a higher rank)
        //White pawns can move up the board (to squares of a lower rank)
        //Pawn forward moves can only be to unoccupied squares
        //Pawn diagonal moves can only be to occupied squares (except for en passant)
        //Pawn forward moves keep the same file and change their rank by +- 1
        //Pawn diagonal moves change both the file and rank +-1
        //white pawns on rank 1 can promote on the next move if they have a square to go to
        //black pawns on rank 6 can promote on the next move if they have a square to go to
        //Pawns can double move on their first move(if they are on their starting rank)
        boolean firstSquareOccupied = false;
        for (int i = 0; i < candidateTargetSquares.size(); i++) {
            Tile ghostTile = candidateTargetSquares.get(i);
            if (ghostTile.isInBounds()) {
                Tile boardTile = boardTiles[ghostTile.getRank()][ghostTile.getFile()];
                switch (i) {
                    //Here, we are looking at the one forward pawn move
                    case 0 -> {
                        //if the square we want to move to is unoccupied, we can go there
                        if (boardTile.isOccupied()) {
                            firstSquareOccupied = true;
                            continue;
                        }
                        //if we are one rank away from promoting, this move is also a pawn promotion
                        if (isWhite ? rank == 1 : rank == 6) {
                            for (String pieceName : GameBoard.pawnPromotionPieceNames) {
                                Move forwardPromotion = new Move(thisTile, boardTile, false, pieceName);
                                addPseudolegalMove(board, forwardPromotion);
                            }
                        } else {
                            Move oneForwardMove = new Move(thisTile, boardTile, false);
                            addPseudolegalMove(board, oneForwardMove);
                        }
                    }
                    //Here, we are looking at the two forward pawn move
                    case 1 -> {
                        //if the pawn is not on its starting rank, we cannot do this move
                        if (!(isWhite ? rank == 6 : rank == 1)) {
                            continue;
                        }
                        //if the square one forward or the square two forward is occupied, we cannot do this move
                        if (boardTile.isOccupied() || firstSquareOccupied) {
                            continue;
                        }
                        //two forward pawn moves will never be pawn promotions so we do not have to check
                        legalMoves.add(new Move(thisTile, boardTile, false));

                    }
                    //Here we are looking at the diagonal pawn move. En passant is a possibility
                    //we have to keep in mind that en passant can reveal a check, even if the en passant
                    //capture piece is not pinned.
                    //TODO: add unmake moves to check for this scenario
                    //TODO: first, add a set of tiles of all enemy targeted tiles in gameboard
                    case 2, 3 -> {
                        if (!(boardTile.isOccupied())) {
                            if (GameBoard.getLetterSquareCombination(rank, file)
                                    .equals(board.getEnPassantTargetSquare())) {
                                Move enPassant = new Move(thisTile, boardTile);
                                addPseudolegalMove(board, enPassant);
                                continue;
                            }
                        }
                        //if we are here, we are looking at a standard diagonal pawn capture
                        Piece piece = boardTile.getPiece();
                        if (piece.isWhite() != this.isWhite) {
                            if (isWhite ? rank == 1 : rank == 6) {
                                for (String pieceName : GameBoard.pawnPromotionPieceNames) {
                                    Move diagonalPawnPromotion = new Move(thisTile, boardTile, true, pieceName);
                                    addPseudolegalMove(board, diagonalPawnPromotion);
                                }
                            } else {
                                Move diagonalCapture = new Move(thisTile, boardTile, true);
                                addPseudolegalMove(board, diagonalCapture);
                            }
                        }
                    }
                }
            }
        }
        return this.legalMoves;
    }
}

//The only chance of a move that a pinned pawn has is capturing the pinner
//        if (isPinned) {
//            //if the piece is pinned diagonally our pawn is moving towards the king on the chess board,
//            //then we have no moves. We cannot move forward or capture diagonally
//            //If the pawn is moving away from the king, there is a chance for capture.
//            //The only move is if the pinner can be diagonally captured by us.
//            //This is because capturing it would break the pin
//            if (getPinDirection() == Direction.DIAGONAL) {
//                //pinned white pawns can only capture diagonally if they are on a lower rank than the king
//                //pinned black pawns can only capture diagonally if they are on a higher rank than the king
//                if (isWhite ? kingRank > this.rank : kingRank < this.rank) {
//                    //if we are here, for white, the king is below and to the right of our pawn
//                    //for black, the king is above and to the right of our pawn
//                    if (kingFile > this.file) {
//                        //for white, we can only move up left = rank -1, file -1
//                        //For black pawns, we can only move down and left
//                        //White's up right is black's down left
//                        Tile ghostTile = isWhite ? upLeft : upRight;
//                        if (ghostTile.isInBounds()) {
//                            Tile candidateTile = boardTiles[ghostTile.getRank()][ghostTile.getFile()];
//                            if (candidateTile.isOccupied() && candidateTile.getPiece().isWhite() != this.isWhite) {
//                                //if we are one rank away from promoting, this move is also a pawn promotion
//                                if (isWhite ? rank == 1 : rank == 6) {
//                                    for (String pieceName : GameBoard.pawnPromotionPieceNames) {
//                                        legalMoves.add(new Move(thisTile, candidateTile, true, pieceName));
//                                    }
//
//                                } else {
//                                    legalMoves.add(new Move(thisTile, candidateTile, true));
//                                }
//                            }
//                        }
//                    }
//                    //if we are here, for white, the king is below and to the left of our pawn
//                    //if we are here, for black the king is above and to the left of our pawn
//                    else if (kingFile < this.file) {
//                        //For white, we can move up right = rank - 1, file + 1
//                        //For black pawns, we can only move down right
//                        //For black, white's down right is black's up left
//                        Tile ghostTile = isWhite ? upRight : upLeft;
//                        if (ghostTile.isInBounds()) {
//                            Tile candidateTile = boardTiles[ghostTile.getRank()][ghostTile.getFile()];
//                            if (candidateTile.isOccupied() && candidateTile.getPiece().isWhite() != this.isWhite) {
//                                //if we are one rank away from promoting, this move is also a pawn promotion
//                                if (isWhite ? rank == 1 : rank == 6) {
//                                    for (String pieceName : GameBoard.pawnPromotionPieceNames) {
//                                        legalMoves.add(new Move(thisTile, candidateTile, true, pieceName));
//                                    }
//                                }
//                            } else {
//                                legalMoves.add(new Move(thisTile, candidateTile, true));
//                            }
//                        }
//                    }
//                }
//            }
//            return legalMoves;
//        }