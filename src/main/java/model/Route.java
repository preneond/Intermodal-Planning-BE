package model;

import java.util.List;

public class Route {
    public List<Leg> legList;

    public Route() {
    }

    public Route(List<Leg> legList) {
        this.legList = legList;
    }
}
