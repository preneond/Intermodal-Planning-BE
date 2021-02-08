package cz.cvut.fel.intermodal_planning.planner.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
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

    public boolean isEmpty() {
        return legList.isEmpty();
    }
}
