package student;

import student.Pieces.King;
import student.Pieces.Pawn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.*;

/**
 * A GUI window to add chess board components to. BoardFrame is a Bill Pugh singleton object.
 * BoardFrame displays the 8 x 8 chess board grid tiles and the pieces. BoardFrame
 * does not display anything until its boardTiles are set by GameBoard
 */
//TODO Add a button and chooseTheme() method. Do this for piece theme too
//TODO: add a method that takes in a tile and updates the images when pieces are moved
public final class BoardFrame extends JFrame implements MouseListener/*, MouseMotionListener*/ {
    //Creates a new JPanel, a GUI component that functions as a container to hold other components
    //Uses GridLayout to make an 8 x 8 grid to add components to.There will be no horizontal or vertical
    // gap in between each tile which, so we pass 0 and 0 for the hgap and vgap parameters.
    private final JPanel boardPanel = new JPanel(new GridLayout(8, 8, 0, 0));
    private final TilePanel[][] boardTilePanels = new TilePanel[8][8];
    private boolean frameInitialized = false;
    private Tile[][] boardTiles;
    private GameBoard gameBoard;

    private Point startPoint;
    private Point endPoint;
    private Piece currentClickedPiece;
    private final Set<TilePanel> currentTargetPanels = new HashSet<>();
    private boolean waitingForPawnPromotionChoice = false;

    //private constructor to prevent instantiation
    private BoardFrame() {
        this.setSize(GameBoard.FRAME_WIDTH, GameBoard.FRAME_HEIGHT); //sets the size of the window
        this.setLayout(new BorderLayout());
        //stops the program and breaks out of the window when the x button is clicked
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null); //centers the frame in the middle of our screen
        this.setResizable(false); //locks in the size of the frame and disallows the user to resize it
        this.setUndecorated(false); //Keeps the borders, and the exit, minimize, and window button
        this.setVisible(true); // displays the window so the user can see it
    }

    /**
     * A static inner class to hold our instance of BoardFrame. Handles instance creation, is threadsafe,
     * and uses lazy initialization
     */
    private static final class BoardFrameHolder {
        private static final BoardFrame instance = new BoardFrame();
    }

    public static BoardFrame getInstance() {
        return BoardFrameHolder.instance;
    }

    /**
     * Sets the BoardFrame's board to the @param board. This is done by the Game Controller,
     * when a game is initially created, which is why this method can only be called once.
     *
     * @param board holds an 8 x 8 grid of boardTiles containing each chess board square and the pieces on them.
     * @throws IllegalArgumentException if boardTiles are null, or the board tiles have already been set.
     */
    public void setBoard(GameBoard board) {
        if (this.gameBoard != null) {
            throw new IllegalArgumentException("BoardTiles have already been set." +
                    " BoardFrame's boardTiles cannot be overwritten. They can only be set once.");
        }
        if (board == null) {
            throw new IllegalArgumentException("Null GameBoard passed to BoardFrame.setBoard().");
        }
        this.gameBoard = board;
        this.boardTiles = board.getBoardTiles();
        displayInitialPosition();
    }

    public ImageIcon getImageIcon(Piece piece) {
        String imagePath = "images/" + piece.getPieceId() + ".png";
        File file = new File(imagePath);
        if (!(file.exists())) {
            throw new RuntimeException("unable to read file " + imagePath);
        }
        return new ImageIcon(imagePath);
    }

    private void displayInitialPosition() {
        //we need to draw 8 x 8 squares, using a for loop
        for (int rank = 0; rank < GameBoard.NUM_RANKS; rank++) {
            for (int file = 0; file < GameBoard.NUM_FILES; file++) {
                Tile currentTile = boardTiles[rank][file];
                TilePanel tilePanel = new TilePanel(currentTile);
                //At this point, the tileLabel has a color, and a dimension. We just need to set its piece
                boardTilePanels[rank][file] = tilePanel;
                boardPanel.add(tilePanel);
            }
        }
        boardPanel.setVisible(true);
        this.add(boardPanel);
        frameInitialized = true;
        this.pack();
        this.setVisible(true);
    }

    private void makeMove(Move move) {
        gameBoard.makeActualMove(move);
        this.boardTiles = gameBoard.getBoardTiles();
        updateDisplayAfterMove(move);
    }


    public void updateDisplayAfterMove(Move move) {
//        //TODO: implement
//        Tile startingTile = move.getStartingTile();
//        Tile targetTile = move.getTargetTile();
//        Tile victimTile = move.getVictimTile();
//        TilePanel startingTilePanel = boardTilePanels[startingTile.getRank()][startingTile.getFile()];
//        startingTilePanel.updateView();
//        boardTilePanels[targetTile.getRank()][targetTile.getFile()].updateView();
//        boardTilePanels[victimTile.getRank()][victimTile.getFile()].updateView();
        for (int rank = 0; rank < GameBoard.NUM_RANKS; rank++) {
            for (int file = 0; file < GameBoard.NUM_FILES; file++) {
                boardTilePanels[rank][file].updateView();
            }
        }
        boardPanel.revalidate();
        boardPanel.setVisible(true);
        this.revalidate();
        this.pack();
        for (TilePanel targetPanel : currentTargetPanels) {
            //resets all the old target tiles to their original states after a new piece is clicked
            for (Component component : targetPanel.getComponents()) {
                if ((component instanceof JLabel) || (component instanceof JPanel && !(component instanceof TilePanel))) {
                    //Does not remove the TilePanel since the tilePanel container can hold future pieces.
                    //Removes the JPanel holding the target tile circle marking
                    //Removes the JLabel holding the image and mouseListener
                    //Removes the mouse listener so we no longer listens for mouse clicks on the target tile
                    targetPanel.remove(component);
                    targetPanel.updateView();
                }
            }
        }
        this.currentClickedPiece = null;
        this.currentTargetPanels.clear();

    }


    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     *
     * @param e the event to be processed
     */
    //If the mouse is pressed, moved, and then released, it does not count as a click for some reason
    //released gets checked first before it counts as a click
    @Override
    public void mouseClicked(MouseEvent e) {
        Object source = e.getSource();
        if (source instanceof ImageLabel sourceImageLabel) {
            //if we are here, a piece was clicked on
            TilePanel sourceTilePanel = sourceImageLabel.getTilePanel();
            Tile tile = boardTiles[sourceTilePanel.getRank()][sourceTilePanel.getFile()];
            if (tile == null || !tile.isOccupied()) {
                throw new IllegalArgumentException("An error occurred. " +
                        "Inconsistency between GUI tile array and backend Tile array when " +
                        "Tile " + tile + " is clicked on ");
            }
            //After a new click on a piece happens, we must clear the old target panels of their markings
            //If the same piece is clicked twice, we no longer want to move it. We can take away its target tiles
            //If a different piece is clicked, we still want to get rid of the old piece's target tiles, as we still
            //no longer want to moe the old piece
            for (TilePanel targetPanel : currentTargetPanels) {
                //resets all the old target tiles to their original states after a new piece is clicked
                for (Component component : targetPanel.getComponents()) {
                    if ((component instanceof JLabel) || (component instanceof JPanel && !(component instanceof TilePanel))) {
                        //Does not remove the TilePanel since the tilePanel container can hold future pieces.
                        //Removes the JPanel holding the target tile circle marking
                        //Removes the JLabel holding the image and mouseListener
                        //Removes the mouse listener so we no longer listens for mouse clicks on the target tile
                        targetPanel.remove(component);
                        targetPanel.updateView();
                    }
                }
            }
            this.currentTargetPanels.clear();
            //if the same piece is clicked on twice in a row, we don't want to move it anymore.
            // No need to recalculate the target tiles, we can just return out of this method
            if (this.currentClickedPiece != null && this.currentClickedPiece.equals(tile.getPiece())) {
                return;
            }
            //if we are here, a new piece was clicked on, the player changed their mind on what piece to move
            //we need to set the update the UI of the target tiles of the new piece to move
            this.currentClickedPiece = tile.getPiece();
            Set<Tile> currentTargetTiles = currentClickedPiece.getTargetTiles(gameBoard);
            //For each tile in the piece's target tiles where it can move to, let's mark it with a circle
            //This is to let the player know they can move there.
            //We will do so by adding a panel to the target tile panel with a painted-on marking
            for (Tile targetTile : currentTargetTiles) {
                TilePanel targetPanel = boardTilePanels[targetTile.getRank()][targetTile.getFile()];
                JPanel targetTileImagePanel;
                if (!(tile.isOccupied())) {
                    //if we have an occupied tile, mark it with a black circle
                    targetTileImagePanel = new JPanel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            g.setColor(Color.gray);
                            g.drawOval(0, 0, 48, 48);
                        }
                    };
                } else {
                    //if we have an occupied tile, we can't just draw a black circle, it will cover the piece
                    //Instead, let's draw a ring that will encapsulate the piece, to show it can be captured
                    targetTileImagePanel = new JPanel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            g.setColor(Color.gray);
                            g.fillOval(0, 0, getWidth(), getHeight());
                        }
                    };
                }
                //Let's make the marking visible on the target tile
                targetPanel.setLayout(null);
                targetTileImagePanel.setSize(32, 32);
                targetTileImagePanel.setOpaque(false); //Sets background color outside the circle to transparent
                targetTileImagePanel.setLocation(new Point(16, 16)); //adds the black dot to the center of the tile
                targetPanel.add(targetTileImagePanel); //adds the panel holding the marking to the tile panel
                targetPanel.repaint(); //actually displays the markings for the player to see
                targetPanel.addMouseListener(this); //listens for moves to target tiles so we can make a move there
                this.currentTargetPanels.add(targetPanel);
            }
        } else if (source instanceof TilePanel destinationPanel) {
            if (currentClickedPiece == null || !currentTargetPanels.contains(destinationPanel)) {
                return;
            }
            Tile startingTile = currentClickedPiece.tile;
            Piece pawnPromotionPiece = null;
            Tile destinationTile = destinationPanel.getTile();
            String destinationID = GameBoard.getLetterSquareCombination(
                    destinationTile.getRank(), destinationTile.getFile());
            boolean isPawnPromotion = false;
            boolean isCapture = destinationTile.isOccupied();
            if (currentClickedPiece instanceof Pawn currentPawn) {
                if ((currentPawn.isWhite && destinationTile.getRank() == 0) || (!currentPawn.isWhite && destinationTile.getRank() == 7)) {
                    pawnPromotionPiece = getPawnPromotionPieceChoice(destinationTile);
                    isPawnPromotion = true;
                } else if (destinationID.equals(gameBoard.getEnPassantTargetSquare())) {
                    //make en passant move
                    makeMove(new Move(startingTile, destinationTile));
                    return;
                }
            } else if (currentClickedPiece instanceof King) {
                if (Math.abs(startingTile.getFile() - destinationTile.getFile()) > 1) {
                    //if the king is moving more than one tile, then we have a castling move on our hands
                    if (destinationTile.equals(King.whiteKingsideCastleTile)
                            || destinationTile.equals(King.blackKingsideCastleTile)) {
                        //make kingside castling move
                        this.makeMove(new Move(startingTile, true));
                        return;
                    } else if (destinationTile.equals(King.whiteQueensideCastleTile)
                            || destinationTile.equals(King.blackQueensideCastleTile)) {
                        //make queenside castling move
                        this.makeMove(new Move(startingTile, false));
                        return;
                    } else {
                        throw new IllegalArgumentException("Error occured when moving " +
                                currentClickedPiece + " to " + destinationTile);
                    }
                }
            }
            if (isPawnPromotion) {
                this.makeMove(new Move
                        (startingTile, destinationTile, isCapture, pawnPromotionPiece.getClass().getSimpleName()));
            } else {
                this.makeMove(new Move(startingTile, destinationTile, isCapture));
            }
        }
    }

    private Piece getPawnPromotionPieceChoice(Tile promotionTile) {
        //TODO: display pawn promotion pieces and force the player to pick one
        this.waitingForPawnPromotionChoice = true;
        //display pawn promotion piece options
        //wait until one is picked, get the piece
        //Until one is clicked, disallow the player to click elsewhere
        this.waitingForPawnPromotionChoice = false;
        return null;
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        startPoint = null;
    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseExited(MouseEvent e) {

    }

//    /**
//     * Invoked when a mouse button is pressed on a component and then
//     * dragged.  {@code MOUSE_DRAGGED} events will continue to be
//     * delivered to the component where the drag originated until the
//     * mouse button is released (regardless of whether the mouse position
//     * is within the bounds of the component).
//     * <p>
//     * Due to platform-dependent Drag&amp;Drop implementations,
//     * {@code MOUSE_DRAGGED} events may not be delivered during a native
//     * Drag&amp;Drop operation.
//     *
//     * @param e the event to be processed
//     */
//    @Override
//    public void mouseDragged(MouseEvent e) {
//        endPoint = e.getPoint();
////        SwingUtilities.convertPoint(null,e.getPoint(),null);
//        if (this.getBounds().contains(endPoint)) {
//            startPoint.translate(endPoint.x - startPoint.x, endPoint.y - startPoint.y);
//            endPoint.x = Math.max(startPoint.x, 0);
//            endPoint.y = Math.max(startPoint.y, 0);
//        }
//    }
//
//    /**
//     * Invoked when the mouse cursor has been moved onto a component
//     * but no buttons have been pushed.
//     *
//     * @param e the event to be processed
//     */
//    @Override
//    public void mouseMoved(MouseEvent e) {
//
//    }
}
