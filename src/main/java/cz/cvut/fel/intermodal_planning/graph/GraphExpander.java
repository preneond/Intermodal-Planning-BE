package cz.cvut.fel.intermodal_planning.graph;

import cz.cvut.fel.intermodal_planning.subplanners.adapters.PlannerAdapter;
import cz.cvut.fel.intermodal_planning.planner.model.LocationArea;
import cz.cvut.fel.intermodal_planning.planner.model.Route;
import cz.cvut.fel.intermodal_planning.planner.model.TransportMode;

import java.util.List;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public interface GraphExpander {

    List<Route> expandGraphFromKnownRequests(int numOfRequests);

    //Uninformed strategies
    List<Route> expandGraphByRandomOD(int numOfRequests, PlannerAdapter plannerAdapter,
                                      LocationArea locationArea, TransportMode mode);

    List<Route> expandGraphByChainingRandomOD(int numOfRequests, PlannerAdapter plannerAdapter,
                                              LocationArea locationArea, TransportMode mode);

    List<Route> expandGraphByRandomODWithMinDistanceBetween(int numOfRequests, PlannerAdapter plannerAdapter,
                                                            LocationArea locationArea, TransportMode mode);

    //Informed strategies
    List<Route> expandGraphByFillingMinNodesAreaNormDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                         LocationArea locationArea);

    List<Route> expandGraphByFillingMinNodesAreaUnifDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                         LocationArea locationArea);

    List<Route> expandGraphByFillingMinEdgesAreaUnifDist(int numOfRequests, LocationArea locationArea, TransportMode mode);

    List<Route> expandGraphByFillingMinEdgesAreaNormDist(int numOfRequests, LocationArea locationArea, TransportMode mode);

    //Supervised strategies
    List<Route> expandGraphUsingKnownNodesAsOD(int numOfRequests, PlannerAdapter plannerAdapter,
                                               LocationArea locationArea, TransportMode mode);

}
