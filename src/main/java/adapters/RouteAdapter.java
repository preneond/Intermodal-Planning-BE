package adapters;

import model.Coordinate;
import model.Leg;

import java.util.List;

public abstract class RouteAdapter {
    public List<Leg> legList;
    public Coordinate endPoint;
    public Coordinate startPoint;

    public  RouteAdapter(){
    }

    public RouteAdapter(Coordinate startPoint, Coordinate endPoint,List<Leg> legList) {
        this.legList = legList;
        this.endPoint = endPoint;
        this.startPoint = startPoint;
    }
}
