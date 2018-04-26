package cz.cvut.fel.intermodal_planning.model.planner;

import java.awt.*;
import java.io.Serializable;

public enum TransportMode implements Serializable {
    WALK, BICYCLE, TRANSIT, CAR, UNKNOWN;

    public Color modeColor() {
        switch (this) {
            case CAR:
                return Color.BLUE;
            case BICYCLE:
                return Color.GREEN;
            case WALK:
                return Color.YELLOW;
            case TRANSIT:
                return Color.RED;
            default:
                return Color.BLACK;
        }
    }

    public static TransportMode[] availableModes() {
        return new TransportMode[]{TransportMode.CAR, TransportMode.TRANSIT, TransportMode.BICYCLE, TransportMode.WALK};
    }

    public String shortcut() {
        return this.name().substring(0, 1);
    }

    public static TransportMode[] singleModalModes() {
        return new TransportMode[]{TRANSIT, CAR, BICYCLE};
    }
}
