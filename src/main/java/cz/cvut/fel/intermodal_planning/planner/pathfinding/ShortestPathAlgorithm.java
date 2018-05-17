package cz.cvut.fel.intermodal_planning.planner.pathfinding;

import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import cz.cvut.fel.intermodal_planning.planner.RoutePlanner;
import cz.cvut.fel.intermodal_planning.graph.model.GraphEdge;
import cz.cvut.fel.intermodal_planning.planner.model.Location;
import cz.cvut.fel.intermodal_planning.planner.model.TransportMode;
import cz.cvut.fel.intermodal_planning.general.utils.LocationUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ShortestPathAlgorithm<TNode extends Node> {
    private final Graph<TNode, GraphEdge> graph;

    private HashSet<Integer> closedList;
    private FibonacciHeap<TNode> openList;
    private List<GraphEdge> path;
    private Map<Integer, Integer> prevNodes;


    public ShortestPathAlgorithm(Graph<TNode, GraphEdge> graph) {
        this.graph = graph;

        openList = new FibonacciHeap<>();
        closedList = new HashSet<>();
        path = new ArrayList<>();
        prevNodes = new HashMap<>();
    }

    /**
     * Finding path in model using Dijkstra's algorithm - Many to many
     *
     * @param origin Origin Location
     * @param destination Destination Location
     * @param originNodes List of Nodes, FROM which the path is searched
     * @param destinationNodes List of Nodes, TO which the path is searched
     * @param availableModes List of transport modes, which are allowed to use
     * @return Edge sequence representing the model path
     */
    public List<GraphEdge> plan(Location origin, Location destination, List<TNode> originNodes, List<TNode> destinationNodes,
                                TransportMode... availableModes) {
        List availableModesList = Arrays.asList(availableModes);
        FibonacciHeap.Entry<TNode> entry_from;
        FibonacciHeap.Entry<TNode> entry_old;

        TNode node_to;
        TreeMap<Long, List<GraphEdge>> pathTreeMap = new TreeMap<>();
        List<GraphEdge> list;
        double priority_new;
        TransportMode prevMode = null;

        openList = new FibonacciHeap<>();
        closedList.clear();
        path.clear();
        prevNodes.clear();

        originNodes.stream().forEach(originNode -> openList.enqueue(originNode,
                RoutePlanner.getDistanceDuration(TransportMode.WALK, LocationUtils.distance(LocationUtils.getNodeLocation(originNode), origin))));

        while (!openList.isEmpty()) {
            entry_from = openList.dequeueMin();

            //we find the DESTINATION NODE! Now, we have to backtrack the path
            if (destinationNodes.contains(entry_from.getValue())) {
                List<GraphEdge> path = findPath(graph, originNodes, entry_from.getValue());
                if (!path.isEmpty()) {
                    GraphEdge lastEdge = path.get(path.size() - 1);
                    TransportMode lastMode = lastEdge.transportMode;
                    double distance = LocationUtils.distance(LocationUtils.getNodeLocation(graph.getNode(lastEdge.toId)), destination);
                    long destinationPenalty = RoutePlanner.getDistanceDuration(lastMode, distance);

                    Long duration = path.stream()
                            .mapToLong(graphEdge -> graphEdge.durationInSeconds)
                            .sum() + destinationPenalty;
                    pathTreeMap.put(duration, path);
                }

                destinationNodes.remove(entry_from.getValue());
                if (destinationNodes.isEmpty()) break;
            }

            closedList.add(entry_from.getValue().id);

            //there is an option, that list of outcoming edges is empty- that's why there is try/catch
            try {
                list = graph.getOutEdges(entry_from.getValue().id);

                // loop all edges from dequeued node
                for (GraphEdge edge : list) {
                    //if node is in closed list
                    // or is not allowed to use a specified transport transportMode then continue
                    // or is not transfer possible

                    if (prevNodes.get(edge.fromId) != null) {
                        prevMode = graph.getEdge(prevNodes.get(edge.fromId), edge.fromId).transportMode;
                    }

                    if (closedList.contains(edge.toId)
                            || !availableModesList.contains(edge.transportMode)
                            || !RoutePlanner.isTransferPossible(prevMode, edge.transportMode)
                            ) {
                        continue;
                    }
                    node_to = graph.getNode(edge.toId);

                    int transferPenalty = (prevMode == null || prevMode == edge.transportMode) ? 0 : RoutePlanner.getTransferPenalty(prevMode);

                    // get cost of start node, we substract start-node's distance and after that we add end-node's distance
                    // and we also add edge length divided by allowed speed
                    priority_new = entry_from.getPriority() + edge.durationInSeconds + transferPenalty;

                    entry_old = openList.getEntry(node_to);
                    //node is in the open list, so we have to compare priority and choose the better ones
                    if (entry_old != null) {
                        if (entry_old.getPriority() > priority_new) {
                            openList.decreaseKey(entry_old, priority_new);
                            prevNodes.put(node_to.id, entry_from.getValue().id);
                        }
                    } else {
                        //if node is not in the open list, then we have to add it there
                        prevNodes.put(node_to.id, entry_from.getValue().id);
                        // do something
                        openList.enqueue(node_to, priority_new);
                    }
                }
            } catch (NullPointerException ignored) {
            }
        }

        return pathTreeMap.isEmpty() ? null : pathTreeMap.firstEntry().getValue();
    }

    /**
     * Finding path in model using all possible transport modes using Dijkstra's algorithm - Many to many
     *
     * @param origin Origin Location
     * @param destination Destination Location
     * @param originNodes List of Nodes, FROM which the path is searched
     * @param destinationNodes List of Nodes, TO which the path is searched
     * @return Edge sequence representing the model path
     */
    public List<GraphEdge> plan(Location origin, Location destination, List<TNode> originNodes, List<TNode> destinationNodes) {
        return plan(origin, destination, originNodes, destinationNodes, TransportMode.availableModes());

    }

    private List<GraphEdge> findPath(Graph<TNode, GraphEdge> graph, List<TNode> originList, TNode destination) {
        LinkedList<GraphEdge> path = new LinkedList<>();
        int tmpId = destination.id;

        List<Integer> originIdList = originList.stream().map(origin -> origin.id).collect(Collectors.toList());

        while (!originIdList.contains(tmpId)) {
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