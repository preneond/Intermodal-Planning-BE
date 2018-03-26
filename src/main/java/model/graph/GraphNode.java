package model.graph;

import com.umotional.basestructures.Node;
import model.planner.TransportMode;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class GraphNode extends Node implements Serializable {
    public Set<TransportMode> ingoingModes;
    public Set<TransportMode> outgoingModes;

    public GraphNode(int id, long sourceId, int latE6, int lonE6, int latProjectedE3, int lonProjectedE3, int elevationInMM) {
        super(id, sourceId, latE6, lonE6, latProjectedE3, lonProjectedE3, elevationInMM);
        ingoingModes = new HashSet<>();
        outgoingModes = new HashSet<>();
    }

    public GraphNode(int id, long sourceId, double lat, double lon, int latProjectedE3, int lonProjectedE3, int elevationInMM) {
        super(id, sourceId, lat, lon, latProjectedE3, lonProjectedE3, elevationInMM);
        ingoingModes = new HashSet<>();
        outgoingModes = new HashSet<>();
    }
}
