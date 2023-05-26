package student;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class TilePanel extends JPanel {
    private final Tile tile;
    private final int rank;
    private final int file;
    private final Color baseBackground;
    private static final BoardFrame boardFrame;

    static {
        boardFrame = BoardFrame.getInstance();
    }

    public TilePanel(Tile tile) {
        this.tile = tile;
        this.rank = tile.getRank();
        this.file = tile.getFile();
        this.baseBackground = (rank + file) % 2 == 0 ? GameBoard.LIGHT_SQUARE_COLOR : GameBoard.DARK_SQUARE_COLOR;
        this.setBackground(baseBackground);
        this.setPreferredSize(new Dimension(64, 64));
        this.setOpaque(true); //ensures that the background color is visible
        if (tile.isOccupied()) {
            Piece piece = tile.getPiece();
            ImageIcon imageIcon = boardFrame.getImageIcon(piece);
            ImageLabel imageLabel = new ImageLabel(this);
            imageLabel.setIcon(imageIcon);
            imageLabel.setSize(new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight()));
            this.add(imageLabel);
            imageLabel.addMouseListener(boardFrame);
        }
    }

    public void updateView() {
        if (tile.isOccupied()) {
            Piece piece = tile.getPiece();
            ImageIcon imageIcon = boardFrame.getImageIcon(piece);
            ImageLabel imageLabel = new ImageLabel(this);
            imageLabel.setIcon(imageIcon);
            imageLabel.setSize(new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight()));
            this.add(imageLabel);
            imageLabel.addMouseListener(boardFrame);
        } else {
            for (Component component : this.getComponents()) {
                if ((component instanceof JLabel imageLabel)) {
                    imageLabel.removeAll();
                    this.remove(component);
                }
            }
        }
        this.revalidate();
        this.repaint();
    }


    public int getRank() {
        return this.tile.getRank();
    }

    public int getFile() {
        return this.tile.getFile();
    }

    public Tile getTile() {
        return this.tile;
    }

    public Color getBaseBackground() {
        return this.baseBackground;
    }

    @Override
    public String toString() {
        return String.format("Tile panel with tile %s", tile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tile);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TilePanel otherTilePanel) {
            return otherTilePanel.tile.equals(this.tile);
        }
        return false;
    }
}
