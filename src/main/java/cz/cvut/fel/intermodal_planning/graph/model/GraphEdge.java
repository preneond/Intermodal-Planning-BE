package cz.cvut.fel.intermodal_planning.graph.model;

import com.umotional.basestructures.Edge;
import cz.cvut.fel.intermodal_planning.planner.model.Location;
import cz.cvut.fel.intermodal_planning.planner.model.TransportMode;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public class GraphEdge extends Edge implements Serializable {
    public TransportMode transportMode;
    public List<Location> polyline;
    public long durationInSeconds;

    public GraphEdge(int fromId, int toId, int lengthInMeters) {
        super(fromId, toId, lengthInMeters);
    }

    @Override
    public String toString() {
        return "(" + fromId + "->" + transportMode.name() + "->" + toId + ')';
    }
}
