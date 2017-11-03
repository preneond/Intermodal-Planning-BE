import model.planner.Route;

import java.util.List;

public class GraphBuilder {
    private static GraphBuilder sharedInstance;

    public static GraphBuilder getInstance() {
        if (sharedInstance == null){
            sharedInstance = new GraphBuilder();
        }
        return sharedInstance;
    }


    public void buildGraph(List<Route> routeList){

    }

    public void buildGraph(Route route){

    }
}
