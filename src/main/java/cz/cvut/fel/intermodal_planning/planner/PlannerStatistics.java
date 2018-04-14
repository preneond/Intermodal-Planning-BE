package cz.cvut.fel.intermodal_planning.planner;

import cz.cvut.fel.intermodal_planning.general.Main;
import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.Location;
import cz.cvut.fel.intermodal_planning.model.planner.Route;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;
import cz.cvut.fel.intermodal_planning.utils.SerializationUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlannerStatistics {
    private static final Logger logger = LogManager.getLogger(PlannerStatistics.class);
    private static Map<String, Integer> histogramMap;

    public static void doComparision(PlannerInitializer plannerInitializer) {

        logger.info("doComparision called");
        histogramMap = new HashMap<>();
        final int loopCount = 1000;

        Main.EXTENDED = false;
        for (int i = 0; i < loopCount; i++) {
//            comparePath(plannerInitializer.perfectRoutePlanner, i + 1);
            compareKnownPath(plannerInitializer.perfectRoutePlanner, i + 1);
//                System.out.println(i);
        }

        System.out.println("Avg duration: " + Storage.INTERMODAL_AVG_DURATION / loopCount);

        SerializationUtils.writeObjectToFile(stringify(histogramMap),
                new File(Storage.STATISTICS_PATH + "/histogram.txt"));

        Main.EXTENDED = true;
        histogramMap = new HashMap<>();
        Storage.INTERMODAL_AVG_DURATION = 0;
        for (int i = 0; i < loopCount; i++) {
//            comparePath(plannerInitializer.extendedRoutePlanner, i + 1);
            compareKnownPath(plannerInitializer.extendedRoutePlanner, i + 1);
        }
        System.out.println("Avg ext duration: " + Storage.INTERMODAL_AVG_DURATION / loopCount);

        SerializationUtils.writeObjectToFile(stringify(histogramMap),
                new File(Storage.STATISTICS_PATH + "/histogram_ext.txt"));

    }

    private static String stringify(Map<String, Integer> map) {
        final String[] result = {""};

        map.entrySet()
                .stream()
                .sorted((o1, o2) -> o1.getValue() > o2.getValue() ? -1 : 1)
                .forEach(entry -> {
                    result[0] += entry.getKey() + ": " + entry.getValue() + "\n";
                });

        System.out.println(result[0]);
        return result[0];
    }

    private static void compareKnownPath(RoutePlanner routePlanner, int count) {
        String odPairPath = Main.EXTENDED ? Storage.OD_PAIR_EXT_PATH : Storage.OD_PAIR_PATH;
        File odFile = new File(odPairPath + "pair_" + count + ".txt");
        Location[] odPair = SerializationUtils.readODPairFromGson(odFile);

        List<GraphEdge> perfectIntermodalPath = routePlanner.findPath(odPair[0], odPair[1]);

        if (perfectIntermodalPath == null) return;

        Storage.INTERMODAL_AVG_DURATION += routePlanner.getDuration(perfectIntermodalPath);
        addToHistogram(perfectIntermodalPath);
    }

    private static void comparePath(RoutePlanner routePlanner, int count) {
        Location[] odPair = null;
        List<GraphEdge> intermodalPath = null;

        while (intermodalPath == null) {
            odPair = Location.generateRandomLocationsInPrague(2);
            intermodalPath = routePlanner.findPath(odPair[0], odPair[1]);
        }

        storeODPair(odPair, count);

        addToHistogram(intermodalPath);
        Storage.INTERMODAL_AVG_DURATION += routePlanner.getDuration(intermodalPath);
    }

    private static void storeODPair(Location[] pair, int count) {
        String odPairPath = Main.EXTENDED ? Storage.OD_PAIR_EXT_PATH : Storage.OD_PAIR_PATH;
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
        String description = "";
        if (intermodalPath == null || intermodalPath.isEmpty()) return description;


        long curDuration = intermodalPath.get(0).durationInSeconds;
        TransportMode curMode = intermodalPath.get(0).mode;
        GraphEdge curEdge;
        for (int i = 1; i < intermodalPath.size(); i += 1) {
            curEdge = intermodalPath.get(i);
            if (curMode == curEdge.mode) {
                curDuration += curEdge.durationInSeconds;
            } else {
                description += curDuration + curMode.shortcut() + "+";
                curMode = curEdge.mode;
                curDuration = curEdge.durationInSeconds;
            }
        }
        description += curDuration + curMode.shortcut();

        return description;
    }


    private static void makeGraphQualityDescription(GraphMaker graphMaker) {
        RoutePlanner routePlanner = new RoutePlanner(graphMaker);

        double sizeDeviation;
        int findingPathCount = 100;

        long[] routeDuration = new long[findingPathCount];
        long[] refinementRouteDuration = new long[findingPathCount];
        long[] deviation = new long[findingPathCount];

        for (int j = 0; j < findingPathCount; j++) {
            List<GraphEdge> graphPath = routePlanner.findRandomPath();
            routeDuration[j] = routePlanner.getDuration(graphPath);
            Route refinementRoute = routePlanner.doRefinement(graphPath);
            refinementRouteDuration[j] = routePlanner.getDuration(refinementRoute);
            deviation[j] = routeDuration[j] - refinementRouteDuration[j];
        }

        sizeDeviation = Arrays.stream(deviation).average().orElse(0);

        //TODO fill num of requests
        File file = new File(Storage.STATISTICS_PATH +
                "/statistics_graph_requests" + Storage.getTotalRequestCount() + ".txt");
        try {
            FileWriter writer = new FileWriter(file, false);
            for (int ii = 0; ii < findingPathCount; ii++) {
                writer.write(routeDuration[ii] + " " + refinementRouteDuration[ii] + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File reqfile = new File(Storage.STATISTICS_PATH + "/requests.txt");
        try {
            FileWriter writer = new FileWriter(reqfile, true);
            writer.write(10000 + " requests: " + sizeDeviation + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
