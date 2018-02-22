package adapters;

import client.HereMapsApiClient;
import model.planner.Location;
import model.planner.Route;
import model.planner.TransportMode;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class HereMapsPlannerAdapter extends PlannerAdapter {
    @Override
    public List<Route> findRoutes(Location origin, Location destination, TransportMode mode, Timestamp arrival) {
        Map<String, Object> response = HereMapsApiClient.sendNewRequest("TODO", "TODO", mode);

        return getRouteList(response);
    }

    private List<Route> getRouteList(Map<String, Object> response) {
        return null;
    }

    @Override
    public List<Route> findRoutes(Location origin, Location destination, TransportMode mode) {
        return null;
    }

    @Override
    public List<Route> findRoutes(Location origin, Location destination) {
        return null;
    }

    @Override
    public Route findBestRoute(Location origin, Location destination, TransportMode mode, Timestamp arrival) {
        return null;
    }

    @Override
    public Route findBestRoute(Location origin, Location destination, TransportMode mode) {
        return null;
    }

    @Override
    public Route findBestRoute(Location origin, Location destination) {
        return null;
    }
}
