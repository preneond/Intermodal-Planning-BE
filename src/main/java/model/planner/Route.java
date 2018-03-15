package model.planner;

import java.util.ArrayList;
import java.util.List;

public class Route {
    public List<Leg> legList;

    public Route() {
        legList = new ArrayList<>();
    }

    public Route(List<Leg> legList) {
        this.legList = legList;
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
