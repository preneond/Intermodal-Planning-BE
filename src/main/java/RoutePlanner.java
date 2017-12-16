import adapters.GMapsPlannerAdapter;
import adapters.PlannerAdapter;
import model.planner.Location;
import model.planner.Route;
import model.planner.TransportMode;
import utils.GeoJSONBuilder;
import utils.RandomLocationGenerator;

import java.util.ArrayList;
import java.util.List;

public class RoutePlanner {
    PlannerAdapter[] plannerAdapters;

    public RoutePlanner() {
        plannerAdapters = new PlannerAdapter[]{new GMapsPlannerAdapter()};
    }

    public void findRoute() {
        Location[] locArray = RandomLocationGenerator.getInstance().generateLocationsInPrague(5);
        TransportMode mode = TransportMode.CAR;
        List<Route> routes = new ArrayList<>();

        List<Route> routeList = new ArrayList<>();
        for (PlannerAdapter plannerAdapter : plannerAdapters) {
            for (int i=0; i< locArray.length-1;i++) {
                for (int j = i + 1; j < locArray.length; j++) {
                    routeList = plannerAdapter.findRoutes(locArray[i], locArray[j]);
                    routes.addAll(routeList);
                }
            }
        }

        GraphMaker graphMaker = GraphMaker.getInstance();
        graphMaker.createGraph(routeList);

        GeoJSONBuilder geoJSONBuilder = new GeoJSONBuilder();
        geoJSONBuilder.addPolylinesFromRoutes(routes);

        System.out.println(geoJSONBuilder.buildJSONString());
    }
}
