package cz.cvut.fel.intermodal_planning.utils;

import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import cz.cvut.fel.intermodal_planning.adapters.PlannerAdapter;
import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.Leg;
import cz.cvut.fel.intermodal_planning.model.planner.Location;
import cz.cvut.fel.intermodal_planning.model.planner.Route;
import cz.cvut.fel.intermodal_planning.model.planner.Step;

import java.util.ArrayList;
import java.util.List;


public class LocationUtils {
    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     * <p>
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     *
     * @returns Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1, double lon2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }

    public static <TNode extends Node> double distance(TNode n1, TNode n2) {
        return distance(n1.latE6, n2.latE6, n1.lonE6, n2.lonE6);
    }

    public static double distance(Location l1, Location l2) {
        return distance(l1.lat, l2.lat, l1.lon, l2.lon);
    }

    public static Location getNodeLocation(Node node) {
        return new Location(node.getLatitude(), node.getLongitude());
    }

    public static List<Location> getLocationSequence(Route route) {
        List<Location> locationList = new ArrayList<>();
        locationList.add(route.origin);

        for (Leg leg : route.legList) {
            for (Step step : leg.steps) {
                locationList.add(step.startLocation);
                locationList.add(step.endLocation);
            }

        }
        locationList.add(route.destination);
        return locationList;
    }


    public static List<Location> getLocationsFromEdges(List<GraphEdge> edgeList, Graph<Node, GraphEdge> graph) {
        if (edgeList.isEmpty()) return new ArrayList<>();

        List<Location> locationList = new ArrayList<>();
        Node nodeFrom;
        for (GraphEdge edge : edgeList) {
            nodeFrom = graph.getNode(edge.fromId);
            locationList.add(new Location(nodeFrom.getLatitude(), nodeFrom.getLongitude()));
        }
        Node nodeTo = graph.getNode(edgeList.get(edgeList.size() - 1).toId);
        locationList.add(new Location(nodeTo.getLatitude(), nodeTo.getLongitude()));

        return locationList;
    }

}
