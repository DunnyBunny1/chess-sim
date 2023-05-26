package student;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class ImageLabel extends JLabel {
    private final TilePanel tilePanel;

    public ImageLabel(TilePanel startingTilePanel){
        this.tilePanel = startingTilePanel;
    }

    public TilePanel getTilePanel(){
        return this.tilePanel;
    }

    @Override
    public String toString(){
        return String.format("Image Label with TilePanel %s",this.tilePanel);
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.tilePanel);
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof ImageLabel otherLabel){
            return otherLabel.tilePanel.equals(this.tilePanel);
        }
        return false;
    }

}
