package model.graph;

import com.umotional.basestructures.Edge;
import model.planner.Location;
import model.planner.TransportMode;

import java.io.Serializable;
import java.util.List;

public class GraphEdge extends Edge implements Serializable {
    public TransportMode mode;
    public List<Location> polyline;
    public long durationInSeconds;

    public GraphEdge(int fromId, int toId, int lengthInMeters) {
        super(fromId, toId, lengthInMeters);
    }
}
