package adapters;

import model.planner.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;


public abstract class PlannerAdapter {

    public static final float WALKING_SPEED_MPS = 1.4f;

    public abstract List<Route> findRoutes(Location origin, Location destination, TransportMode mode);

    public abstract List<Route> findRoutes(Location origin, Location destination);

    public abstract Route findRoute(Location origin, Location destination, TransportMode mode);

    public abstract Route findRoute(Location origin, Location destination);

}
