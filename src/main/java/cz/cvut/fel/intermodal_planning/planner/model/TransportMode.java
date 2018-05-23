package cz.cvut.fel.intermodal_planning.planner.model;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public enum TransportMode implements Serializable {
    WALK, BICYCLE, TRANSIT, CAR, UNKNOWN;

    /**
     * Mode Color for Frontend App
     * @return
     */
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

    /**
     * Transport modes that are available in our Graph
     *
     * @return mode list
     */
    public static TransportMode[] availableModes() {
        return new TransportMode[]{TransportMode.CAR, TransportMode.TRANSIT, TransportMode.BICYCLE, TransportMode.WALK};
    }

    /**
     * Transport mode shortcut - first letter
     *
     * @return shortcut string
     */
    public String shortcut() {
        return this.name().substring(0, 1);
    }

    /**
     * Singlemodal modes Getter
     *
     * @return mode Array
     */
    public static TransportMode[] singleModalModes() {
        return new TransportMode[]{TRANSIT, CAR, BICYCLE};
    }
}
