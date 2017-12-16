package model.planner;

import java.awt.*;

public enum TransportMode {
    WALK,BIKE,TRANSIT,CAR, UNKNOWN;

    public Color modeColor(){
        switch(this){
            case CAR:
                return Color.BLUE;
            case BIKE:
                return Color.GREEN;
            case WALK:
                return Color.YELLOW;
            case TRANSIT:
                return Color.RED;
            default:
                return Color.BLACK;
        }
    }
}
