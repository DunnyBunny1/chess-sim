package student;

import java.util.Set;

public interface MoveGenerator {
    Set<Move> findLegalMoves(GameBoard board);

}
