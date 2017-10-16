package utils;

// TODO: CHANGE
import com.sun.javafx.sg.prism.NGShape.Mode;

import java.util.List;

public class Leg {
    private Mode mode;
    private List<double[]> coordinates;

    public Leg(Mode mode, List<double[]> coordinates) {
        this.mode = mode;
        this.coordinates = coordinates;
    }

    public Mode getMode() {
        return mode;
    }

    public List<double[]> getCoordinates() {
        return coordinates;
    }
}