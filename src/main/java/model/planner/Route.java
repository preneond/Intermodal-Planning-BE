package model.planner;

import java.util.List;

public class Route {
    public List<Leg> legList;

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
