package cz.cvut.fel.intermodal_planning.planner;

import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.graph.enums.GraphExpansionStrategy;
import cz.cvut.fel.intermodal_planning.graph.enums.GraphQualityMetric;
import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.Location;
import cz.cvut.fel.intermodal_planning.model.planner.LocationArea;
import cz.cvut.fel.intermodal_planning.model.planner.Route;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;
import cz.cvut.fel.intermodal_planning.utils.SerializationUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlannerStatistics {
    private static final Logger logger = LogManager.getLogger(PlannerStatistics.class);
    private static Map<String, Integer> histogramMap;


    public static void doExpansionStrategyComparision(LocationArea area) {
        PlannerInitializer plannerInitializer;
        int[] requestCountArr = new int[]{500, 1000, 2500, 5000, 7500, 10000};

        for (GraphExpansionStrategy expansionStrategy : GraphExpansionStrategy.values()) {
            plannerInitializer = new PlannerInitializer(expansionStrategy, area);
            for (int requestCount : requestCountArr) {
                plannerInitializer.initPlanner(requestCount);
                for (GraphQualityMetric qualityMetric : GraphQualityMetric.values()) {
                    PlannerQualityEvaluator.evaluatePlannerQuality(plannerInitializer, qualityMetric);
                }
            }
        }
    }

    public static void createPlannerStatistics(PlannerInitializer plannerInitializer) {
        logger.info("createPlannerStatistics called");
        histogramMap = new HashMap<>();
        final int loopCount = 1000;

        for (int i = 0; i < loopCount; i++) {
            compareKnownPath(plannerInitializer.routePlanner, i + 1);
        }

        System.out.println("Avg duration: " + Storage.INTERMODAL_AVG_DURATION / loopCount);

        SerializationUtils.writeObjectToFile(stringify(histogramMap),
                new File(Storage.STATISTICS_PATH + "/histogram.txt"));
    }


    private static void compareKnownPath(RoutePlanner routePlanner, int count) {
//        String odPairPath = Main.EXTENDED ? Storage.OD_PAIR_EXT_PATH : Storage.OD_PAIR_PATH;
        String odPairPath = Storage.OD_PAIR_PATH;
        File odFile = new File(odPairPath + "pair_" + count + ".txt");
        Location[] odPair = SerializationUtils.readODPairFromGson(odFile);

        List<GraphEdge> perfectIntermodalPath = routePlanner.findPath(odPair[0], odPair[1]);

        if (perfectIntermodalPath == null) return;

        Storage.INTERMODAL_AVG_DURATION += routePlanner.getDuration(perfectIntermodalPath);
        addToHistogram(perfectIntermodalPath);
    }

    private static void comparePath(RoutePlanner routePlanner, LocationArea locationArea, int count) {
        Location[] odPair = null;
        List<GraphEdge> intermodalPath = null;

        while (intermodalPath == null) {
            odPair = locationArea.generateRandomLocations(2);
            intermodalPath = routePlanner.findPath(odPair[0], odPair[1]);
        }

        storeODPair(odPair, count);

        addToHistogram(intermodalPath);
        Storage.INTERMODAL_AVG_DURATION += routePlanner.getDuration(intermodalPath);
    }


    private static void storeODPair(Location[] pair, int count) {
//        String odPairPath = Main.EXTENDED ? Storage.OD_PAIR_EXT_PATH : Storage.OD_PAIR_PATH;
        String odPairPath = Storage.OD_PAIR_PATH;
        File file = new File(odPairPath + "pair_" + count + ".txt");

        SerializationUtils.writeRequestToGson(pair, file);
    }

    private static void addToHistogram(List<GraphEdge> path) {
        String keyDesc = getHistogramDescription(path.stream()
                .filter(graphEdge -> graphEdge.mode != TransportMode.WALK)
                .collect(Collectors.toList()));

        histogramMap.put(keyDesc, histogramMap.containsKey(keyDesc) ? histogramMap.get(keyDesc) + 1 : 1);
    }

    private static String getHistogramDescription(List<GraphEdge> path) {
        return getIntermodalDescription(path).replaceAll("\\d", "");
    }

    private static String getIntermodalDescription(List<GraphEdge> intermodalPath) {
        StringBuilder description = new StringBuilder();
        if (intermodalPath == null || intermodalPath.isEmpty()) return description.toString();

        long curDuration = intermodalPath.get(0).durationInSeconds;
        TransportMode curMode = intermodalPath.get(0).mode;
        GraphEdge curEdge;
        for (int i = 1; i < intermodalPath.size(); i++) {
            curEdge = intermodalPath.get(i);
            if (curMode == curEdge.mode) {
                curDuration += curEdge.durationInSeconds;
            } else {
                description.append(curDuration).append(curMode.shortcut()).append("+");
                curMode = curEdge.mode;
                curDuration = curEdge.durationInSeconds;
            }
        }
        description.append(curDuration).append(curMode.shortcut());

        return description.toString();
    }

    public static void getPathDescription(Route route, String name) {
        if (route.legList == null || route.legList.isEmpty()) {
            logger.error("Path is empty");
            return;
        }

        long routeDuration = route.legList.stream().mapToLong(o -> o.durationInSeconds).sum();
        logger.info("Duration of " + name + " path: " + routeDuration + " seconds");
        logger.info("Number of transfers: " + route.legList.size());
    }

    public static void getPathDescription(List<GraphEdge> path, String name) {
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
            numOfTransfers += (prevEdge.mode == curEdge.mode) ? 0 : 1;
        }

        long routeDuration = path.stream().mapToLong(o -> o.durationInSeconds).sum();
        logger.info("Duration of " + name + " path: " + routeDuration + " seconds");
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
