package model.planner;

import java.awt.*;
import java.io.Serializable;

public enum TransportMode implements Serializable{
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

    public static TransportMode[] availableModes() {
        return new TransportMode[] {TransportMode.WALK,TransportMode.TRANSIT,TransportMode.CAR};
    }
}
