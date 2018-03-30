package pathfinding;

import com.umotional.basestructures.Edge;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import model.graph.GraphEdge;
import model.planner.TransportMode;
import utils.LocationUtils;

import java.util.*;

public class AStar<TNode extends Node> {

    private final Graph<TNode, GraphEdge> graph;

    private HashSet<Integer> closedList;
    private FibonacciHeap<TNode> openList;
    private List<GraphEdge> path;
    private Map<Integer, Integer> prevNodes;


    public AStar(Graph<TNode, GraphEdge> graph) {
        this.graph = graph;

        openList = new FibonacciHeap<>();
        closedList = new HashSet<>();
        path = new ArrayList<>();
        prevNodes = new HashMap<>();
    }

    public List<GraphEdge> plan(TNode origin, TNode destination, TransportMode... availableModes) {
        List availableModesList = Arrays.asList(availableModes);

        FibonacciHeap.Entry<TNode> entry_from;
        FibonacciHeap.Entry<TNode> entry_old;
        TNode node_to;
        List<GraphEdge> list;
        double edgeDuration;
        double priority_new;
        TransportMode prevMode = null;

        openList = new FibonacciHeap<>();
        closedList.clear();
        path.clear();
        prevNodes.clear();

        openList.enqueue(origin, 0);

        while (!openList.isEmpty()) {
            entry_from = openList.dequeueMin();

            //we find the DESTINATION NODE! Now, we have to backtrack the path
            if (equals(entry_from.getValue(), destination)) {
                return findPath(graph, origin, destination);
            }

            closedList.add(entry_from.getValue().id);

            //there is an option, that list of outcoming edges is empty- that's why there is try/catch
            try {
                list = graph.getOutEdges(entry_from.getValue().id);

                // loop all edges from dequeued node
                for (GraphEdge edge : list) {
                    //if node is in closed list or is not allowed to ride a car then continue
                    if (closedList.contains(edge.toId) || !availableModesList.contains(edge.mode)) {
                        continue;
                    }

                    node_to = graph.getNode(edge.toId);

                    //edge length divided by 1000- We are workin' with kilometres
                    edgeDuration = edge.durationInSeconds;
                    int transferPenalty = 0;
                    if (prevMode != null && prevMode != edge.mode) transferPenalty = 120; //set value

                    // get cost of start node, we substract start-node's distance and after that we add end-node's distance
                    // and we also add edge length divided by allowed speed
                    priority_new = entry_from.getPriority() + edgeDuration + transferPenalty;

                    entry_old = openList.getEntry(node_to);
                    //node is in the open list, so we have to compare priority and choose the better ones
                    if (entry_old != null) {
                        if (entry_old.getPriority() > priority_new) {
                            openList.decreaseKey(entry_old, priority_new);
                            prevNodes.put(node_to.id, entry_from.getValue().id);
                            prevMode = edge.mode;
                        }
                    } else {
                        //if node is not in the open list, then we have to add it there
                        prevNodes.put(node_to.id, entry_from.getValue().id);
                        // do something
                        openList.enqueue(node_to, priority_new);
                        prevMode = edge.mode;
                    }
                }
            } catch (NullPointerException e) {

            }

        }
        return null;
    }

    public List<GraphEdge> plan(TNode origin, TNode destination) {
        return plan(origin, destination, TransportMode.availableModes());

    }

    private List<GraphEdge> findPath(Graph<TNode, GraphEdge> graph, TNode origin, TNode destination) {
        LinkedList<GraphEdge> path = new LinkedList<>();
        int tmpId = destination.id;
        int originId = origin.id;

        while (tmpId != originId) {
            GraphEdge edge = graph.getEdge(prevNodes.get(tmpId), tmpId);
            path.addFirst(edge);
            tmpId = prevNodes.get(tmpId);
        }

        return path;
    }

    public boolean equals(TNode o1, TNode o2) {
        return o1.id == o2.id;
    }
}