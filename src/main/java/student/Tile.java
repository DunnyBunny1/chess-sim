package student;

import javax.swing.*;
import java.awt.*;

//2 tiles are considered equals() if they have the same rank and file
//This is because we use ghost tiles with no pieces when calculating candidate tiles
public class Tile {
    private int file;
    private final int rank;
    private final boolean isLightSquare;
    private Piece piece;

    public Tile(int rank, int file) {
        this(rank, file, null);
    }

    public Tile(int rank, int file, Piece piece) {
        this.rank = rank;
        this.file = file;
        this.piece = piece;
        this.isLightSquare = (rank + file) % 2 == 0;
    }

    public int getFile() {
        return file;
    }

    public int getRank() {
        return rank;
    }

    //never returns null
    public Piece getPiece() {
        if (isOccupied()) {
            return this.piece;
        }
        throw new IllegalArgumentException("Cannot retrieve piece from unoccupied tile");
    }

    public void putPiece(Piece piece) {
        this.piece = piece;
    }

    public boolean isOccupied() {
        return this.piece != null;
    }

    public boolean isInBounds() {
        return this.rank >= 0 && this.rank < 8 && this.file >= 0 && this.file < 8;
    }

    @Override
    public String toString() {
        return String.format(
                (this.isOccupied() ? "Occupied tile with " + piece : "Unoccupied" + " tile at %d, %d "),
                rank, file);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Tile otherTile) {
            return otherTile.rank == this.rank && otherTile.file == this.file;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * GameBoard.HASHING_PRIME_NUMBER + rank;
        hash = hash * GameBoard.HASHING_PRIME_NUMBER + file;
        return hash;
    }
}
