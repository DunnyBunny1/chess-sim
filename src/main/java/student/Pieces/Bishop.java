package student.Pieces;

import student.GameBoard;
import student.Move;
import student.Piece;
import student.SlidingMoveGenerator;

import java.util.Set;

public class Bishop extends Piece implements SlidingMoveGenerator {

    public Bishop(int rank, int file, boolean isWhite) {
        super(rank, file, isWhite);
    }


    @Override
    public Set<Move> findLegalMoves(GameBoard board) {
        if (!this.movesCalculated) {
            this.legalMoves = findSlidingMoves(board, this);
            this.movesCalculated = true;
        }
        return this.legalMoves;
    }


    @Override
    public boolean canMoveVertically() {
        return false;
    }

    @Override
    public boolean canMoveHorizontally() {
        return false;
    }

    @Override
    public boolean canMoveDiagonally() {
        return true;
    }
}
