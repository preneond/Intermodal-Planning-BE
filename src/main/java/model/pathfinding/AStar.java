package model.pathfinding;

import com.umotional.basestructures.Edge;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import utils.LocationUtils;

import java.util.*;

public class AStar<TNode extends Node, TEdge extends Edge> {

    private final Graph<TNode, TEdge> graph;

    HashSet<Integer> closedList;
    FibonacciHeap<TNode> openList;
    List<TEdge> path;
    Map<Integer, Integer> prevNodes;


    public AStar(Graph<TNode, TEdge> graph) {
        this.graph = graph;

        openList = new FibonacciHeap<>();
        closedList = new HashSet<>();
        path = new ArrayList<>();
        prevNodes = new HashMap<>();
    }

    public List<TEdge> plan(TNode origin, TNode destination) {

        FibonacciHeap.Entry<TNode> entry_from;
        FibonacciHeap.Entry<TNode> entry_old;
        TNode node_to;
        List<TEdge> list;
        double edgeLength, distanceFrom, distanceTo;
        double priority_new;

        openList = new FibonacciHeap<>();
        closedList.clear();
        path.clear();
        prevNodes.clear();

        double priority = LocationUtils.distance(origin, destination);
        openList.enqueue(origin, priority);

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
                for (TEdge edge : list) {
                    //if node is in closed list or is not allowed to ride a car then continue
                    if (closedList.contains(edge.toId)) { //|| !edge.getPermittedModes().contains(PermittedMode.CAR)) {
                        continue;
                    }

                    node_to = graph.getNode(edge.toId);

                    //edge length divided by 1000- We are workin' with kilometres
                    edgeLength = edge.length;
//                    edgeSpeed = edge.getAllowedMaxSpeedInKmph();
                    distanceFrom = LocationUtils.distance(entry_from.getValue(), destination);
                    distanceTo = LocationUtils.distance(node_to, destination);

                    // get cost of start node, we substract start-node's distance and after that we add end-node's distance
                    // and we also add edge length divided by allowed speed
                    priority_new = entry_from.getPriority() - distanceFrom + edgeLength + distanceTo;


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
            } catch (NullPointerException e) {

            }

        }
        return null;
    }

    private List<TEdge> findPath(Graph<TNode,TEdge> graph, TNode origin, TNode destination) {
        LinkedList<TEdge> path = new LinkedList<>();
        int tmpId = destination.id;
        int originId = origin.id;

        while (tmpId != originId) {
            TEdge edge = graph.getEdge(prevNodes.get(tmpId), tmpId);
            path.addFirst(edge);
            tmpId = prevNodes.get(tmpId);
        }

        return path;
    }

    public boolean equals(TNode o1, TNode o2) {
        return o1.id == o2.id;
    }
}