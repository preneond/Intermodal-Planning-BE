package cz.cvut.fel.intermodal_planning.model.planner;

import java.util.ArrayList;
import java.util.List;

public class Route {
    public List<Leg> legList;
    public Location origin, destination;

    public Route() {
        legList = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("{ ");
        for (Leg leg : legList) {
            out.append(leg).append(",");
        }
        out.append("}");

        return out.toString();
    }
}
