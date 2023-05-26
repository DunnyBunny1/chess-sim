package student;

public class GameDriver {
    public static void main(String[] args) {
        //calls the game board constructor which initializes the board
        GameBoard gb = new GameBoard(GameBoard.startingPosition);
        BoardFrame frame = BoardFrame.getInstance();
        frame.setBoard(gb);
    }
}
