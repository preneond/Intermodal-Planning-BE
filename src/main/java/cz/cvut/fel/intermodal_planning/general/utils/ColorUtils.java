package cz.cvut.fel.intermodal_planning.general.utils;

import java.awt.*;

public class ColorUtils {
    public static String toHexString(Color color) throws NullPointerException {
        String hexColour = Integer.toHexString(color.getRGB() & 0xffffff);
        if (hexColour.length() < 6) {
            hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
        }
        return "#" + hexColour;
    }
}
