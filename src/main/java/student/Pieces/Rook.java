package student.Pieces;

import student.GameBoard;
import student.Move;
import student.Piece;
import student.SlidingMoveGenerator;

import java.util.Set;

public class Rook extends Piece implements SlidingMoveGenerator {
    public Rook(int rank, int file, boolean isWhite) {
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
        return true;
    }

    @Override
    public boolean canMoveHorizontally() {
        return true;
    }

    @Override
    public boolean canMoveDiagonally() {
        return false;
    }
}
