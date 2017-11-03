package model.planner;

import java.util.List;

public class Route {
    public List<Leg> legList;

    public Route() {
    }

    public Route(List<Leg> legList) {
        this.legList = legList;
    }

    @Override
    public String toString() {
        String out = "{ ";
        for (Leg leg : legList) {
            out += leg + ",";
        }
        out += "}";

        return out;
    }
}
