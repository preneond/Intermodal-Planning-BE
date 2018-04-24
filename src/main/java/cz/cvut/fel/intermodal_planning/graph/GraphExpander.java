package cz.cvut.fel.intermodal_planning.graph;

import cz.cvut.fel.intermodal_planning.adapters.PlannerAdapter;
import cz.cvut.fel.intermodal_planning.model.planner.LocationArea;
import cz.cvut.fel.intermodal_planning.model.planner.Route;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;

import java.util.List;

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

    List<Route> expandGraphByFillingMinNodesAreaEqDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                       LocationArea locationArea);

    List<Route> expandGraphByFillingMinEdgesAreaEqDist(int numOfRequests, LocationArea locationArea, TransportMode mode);

    List<Route> expandGraphByFillingMinEdgesAreaNormDist(int numOfRequests, LocationArea locationArea, TransportMode mode);
    //Supervised strategies

    List<Route> expandGraphUsingKnownNodesAsOD(int numOfRequests, PlannerAdapter plannerAdapter,
                                               LocationArea locationArea, TransportMode mode);

}
