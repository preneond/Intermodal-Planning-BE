import adapters.GMapsPlannerAdapter;
import adapters.PlannerAdapter;
import model.planner.Location;
import model.planner.Route;
import model.planner.TransportMode;
import utils.GeoJSONBuilder;

import java.util.ArrayList;
import java.util.List;

public class RoutePlanner {
    PlannerAdapter[] plannerAdapters;

    public RoutePlanner() {
        plannerAdapters = new PlannerAdapter[]{new GMapsPlannerAdapter()};
    }

    public void findRoute() {
        Location prague = new Location(50.099260, 14.383054);
        Location boleslav = new Location(50.409845, 14.915914);
        TransportMode mode = TransportMode.CAR;
        List<Route> routes = new ArrayList<>();

        List<Route> routeList = new ArrayList<>();
        for (PlannerAdapter plannerAdapter : plannerAdapters) {
            routeList = plannerAdapter.findRoutes(prague, boleslav, mode);
            routes.addAll(routeList);
        }

        GraphMaker graphMaker = GraphMaker.getInstance();
        graphMaker.createGraph(routeList);

        GeoJSONBuilder geoJSONBuilder = new GeoJSONBuilder();
        geoJSONBuilder.addPolylines(routes);

        System.out.println(geoJSONBuilder.buildJSONString());
    }
}
