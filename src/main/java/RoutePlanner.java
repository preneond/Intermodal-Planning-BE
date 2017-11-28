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
        Location dejviceByt = new Location(50.099394, 14.383089);
        Location dejviceSkola = new Location(50.102551, 14.393202);
        Location anickaByt = new Location(50.073639, 14.455722);
        Location karlakSkola = new Location(50.076334, 14.418704);
        Location anickaSkola = new Location(50.074115, 14.474012);

        Location[] locArray = new Location[]{dejviceByt,dejviceSkola,anickaByt,anickaSkola,karlakSkola};
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
        geoJSONBuilder.addPolylines(routes);

        System.out.println(geoJSONBuilder.buildJSONString());
    }
}
