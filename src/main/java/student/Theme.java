package student;

import java.awt.*;

public enum Theme {
    Ocean(/* light blue */ new Color(177, 228, 185), /* dark blue */new Color(112, 162, 163)),
    Classic(Color.WHITE, /*chess.com green color */new Color(118, 150, 86)),
    Navy(Color.WHITE,/* navy blue */ new Color(0, 0, 128));

    public final Color lightSquareColor;
    public final Color darkSquareColor;

    //Enum constructors are private by default
    Theme(Color lightSquareColor, Color darkSquareColor) {
        this.lightSquareColor = lightSquareColor;
        this.darkSquareColor = darkSquareColor;
    }
}
