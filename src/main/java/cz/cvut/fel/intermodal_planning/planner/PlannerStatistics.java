package cz.cvut.fel.intermodal_planning.planner;

import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.graph.model.GraphEdge;
import cz.cvut.fel.intermodal_planning.planner.model.*;
import cz.cvut.fel.intermodal_planning.general.utils.SerializationUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public class PlannerStatistics {
    private static final Logger logger = LogManager.getLogger(PlannerStatistics.class);
    private static Map<String, Integer> histogramMap;


    /**
     * Comparison of path found on abstracted transport network and path using subplanner
     *
     * @param plannerInitializer PlannerInitializer instance
     */
    public static void createRouteComparisonHistogram(PlannerInitializer plannerInitializer) {
        histogramMap = new HashMap<>();
        final int loopCount = 1000;

        for (int i = 0; i < loopCount; i++) {
            compareKnownPath(plannerInitializer.routePlanner, i + 1);
        }

        logger.info("Avg duration: " + Storage.INTERMODAL_AVG_DURATION / loopCount);

        SerializationUtils.writeObjectToFile(stringify(histogramMap),
                new File(Storage.STATISTICS_PATH + "/histogram.txt"));
    }

    /**
     * Comparison of path for which OD pair is stored
     *
     * @param routePlanner RoutePlanner instance
     * @param count number of compared path due to its serialization
     */
    private static void compareKnownPath(RoutePlanner routePlanner, int count) {
        String odPairPath = Storage.OD_PAIR_PATH;
        File odFile = new File(odPairPath + "pair_" + count + ".txt");
        Location[] odPair = SerializationUtils.readODPairFromGson(odFile);

        Route perfectIntermodalPath = routePlanner.metasearchRoute(odPair[0], odPair[1]);

        if (perfectIntermodalPath == null) return;

        Storage.INTERMODAL_AVG_DURATION += routePlanner.getRouteDuration(perfectIntermodalPath);
        addToHistogram(perfectIntermodalPath);
    }

    /**
     * Comparison of path for which OD pair is not stored
     *
     * @param routePlanner RoutePlanner instance
     * @param locationArea Selected Test Region
     * @param pairId ODPair id for its serialization
     */
    private static void comparePath(RoutePlanner routePlanner, LocationArea locationArea, int pairId) {
        Location[] odPair = null;
        Route intermodalPath = null;

        while (intermodalPath == null) {
            odPair = locationArea.generateRandomLocations(2);
            intermodalPath = routePlanner.metasearchRoute(odPair[0], odPair[1]);
        }

        storeODPair(odPair, pairId);

        addToHistogram(intermodalPath);
        Storage.INTERMODAL_AVG_DURATION += routePlanner.getRouteDuration(intermodalPath);
    }

    /**
     * Storing OD Pair for further use
     *
     * @param pair OD pair
     * @param id OD Pair identifier for filename
     */
    private static void storeODPair(Location[] pair, int id) {
        String odPairPath = Storage.OD_PAIR_PATH;
        File file = new File(odPairPath + "pair_" + id + ".txt");

        SerializationUtils.writeRequestToGson(pair, file);
    }

    /**
     * Adding Route to Histogram
     *
     * @param route Route object
     */
    private static void addToHistogram(Route route) {
        route.legList = route.legList.stream()
                .filter(leg -> leg.transportMode != TransportMode.WALK)
                .collect(Collectors.toList());

        String keyDesc = getHistogramDescription(route);

        histogramMap.put(keyDesc, histogramMap.containsKey(keyDesc) ? histogramMap.get(keyDesc) + 1 : 1);
    }

    /**
     * Histogram Description -> dtto as Intermodal Description with removed leg duration
     * @param route Route object
     * @return Stringified Histogram
     */
    private static String getHistogramDescription(Route route) {
        return getIntermodalDescription(route).replaceAll("\\d", "");
    }

    /**
     * Intermodal Path Description
     *
     * Route Example: transit(30s) -> walk(500s) => Desciption:  30T+500W
     * @param intermodalPath
     * @return Description
     */
    private static String getIntermodalDescription(Route intermodalPath) {
        StringBuilder description = new StringBuilder();
        if (intermodalPath == null || intermodalPath.isEmpty()) return description.toString();

        long curDuration = intermodalPath.legList.get(0).durationInSeconds;
        TransportMode curMode = intermodalPath.legList.get(0).transportMode;
        Leg curLeg;
        for (int i = 1; i < intermodalPath.legList.size(); i++) {
            curLeg = intermodalPath.legList.get(i);
            if (curMode == curLeg.transportMode) {
                curDuration += curLeg.durationInSeconds;
            } else {
                description.append(curDuration).append(curMode.shortcut()).append("+");
                curMode = curLeg.transportMode;
                curDuration = curLeg.durationInSeconds;
            }
        }
        description.append(curDuration).append(curMode.shortcut());

        return description.toString();
    }


    /**
     * Route Description to console output
     * @param route  Route Object
     * @param id Identifier
     */
    public static void getRouteDescription(Route route, String id) {
        if (route.legList == null || route.legList.isEmpty()) {
            logger.error("Path is empty");
            return;
        }

        long routeDuration = route.legList.stream().mapToLong(o -> o.durationInSeconds).sum();
        logger.info("Duration of " + id + " path: " + routeDuration + " seconds");
        logger.info("Number of transfers: " + route.legList.size());
    }

    /**
     * Path Description to console output
     * @param path Edge Sequence
     * @param id Identifier
     */
    public static void getPathDescription(List<GraphEdge> path, String id) {
        if (path == null || path.isEmpty()) {
            logger.error("Path is empty");
            return;
        }

        int numOfTransfers = 0;

        GraphEdge curEdge;
        GraphEdge prevEdge;
        for (int i = 1; i < path.size(); i++) {
            prevEdge = path.get(i - 1);
            curEdge = path.get(i);
            numOfTransfers += (prevEdge.transportMode == curEdge.transportMode) ? 0 : 1;
        }

        long routeDuration = path.stream().mapToLong(o -> o.durationInSeconds).sum();
        logger.info("Duration of " + id + " path: " + routeDuration + " seconds");
        logger.info("Number of transfers: " + numOfTransfers);

    }

    private static String stringify(Map<String, Integer> map) {
        final String[] result = {""};

        map.entrySet()
                .stream()
                .sorted((o1, o2) -> o1.getValue() > o2.getValue() ? -1 : 1)
                .forEach(entry -> result[0] += entry.getKey() + ": " + entry.getValue() + "\n");

        System.out.println(result[0]);
        return result[0];
    }


}
