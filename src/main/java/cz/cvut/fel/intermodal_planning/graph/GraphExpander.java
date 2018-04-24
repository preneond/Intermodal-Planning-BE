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
                                      TransportMode mode, LocationArea locationArea);

    List<Route> expandGraphByChainingRandomOD(int numOfRequests, PlannerAdapter plannerAdapter,
                                              TransportMode mode, LocationArea locationArea);

    List<Route> expandGraphByRandomODWithMinDistanceBetween(int numOfRequests, PlannerAdapter plannerAdapter,
                                                            TransportMode mode, LocationArea locationArea);

    //Informed strategies
    List<Route> expandGraphByFillingMinNodesAreaNormDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                         TransportMode mode, LocationArea locationArea);

    List<Route> expandGraphByFillingMinNodesAreaEqDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                       TransportMode mode, LocationArea locationArea);

    List<Route> expandGraphByFillingMinEdgesAreaEqDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                       TransportMode mode, LocationArea locationArea);

    List<Route> expandGraphByFillingMinEdgesAreaNormDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                         TransportMode mode, LocationArea locationArea);
    //Supervised strategies

    List<Route> expandGraphUsingKnownNodesAsOD(int numOfRequests, PlannerAdapter plannerAdapter,
                                               TransportMode mode, LocationArea locationArea);

}
