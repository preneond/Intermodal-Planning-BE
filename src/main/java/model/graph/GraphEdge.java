package model.graph;

import com.umotional.basestructures.Edge;
import model.planner.TransportMode;

public class GraphEdge extends Edge {

    public TransportMode mode;

    public GraphEdge(int fromId, int toId, int lengthInMeters) {
        super(fromId, toId, lengthInMeters);
    }
}
