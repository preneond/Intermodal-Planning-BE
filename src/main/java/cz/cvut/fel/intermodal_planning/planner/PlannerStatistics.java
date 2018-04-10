package cz.cvut.fel.intermodal_planning.planner;

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
        histogramMap = new HashMap<>();

        final int loopCount = 1000;
        for (int i = 0; i < loopCount; i++) {
//            comparePath(plannerInitializer, i + 1);
            compareKnownPath(plannerInitializer, i + 1);
//                System.out.println(i);
        }

        SerializationUtils.writeObjectToFile(stringify(histogramMap),
                new File(Storage.STATISTICS_PATH + "/histogram.txt"));

        logger.info("Car count: " + Storage.CAR_PATH_COUNT);
        logger.info("Transit count: " + Storage.TRANSIT_PATH_COUNT);
        logger.info("Bike count: " + Storage.BIKE_PATH_COUNT);
        logger.info("Intermodal count: " + Storage.INTERMODAL_PATH_COUNT);
        logger.info("Intermodal avg duration: " + Storage.INTERMODAL_AVG_DURATION / loopCount);
    }

    private static String stringify(Map<String, Integer> map) {
        final String[] result = {""};

        map.entrySet()
                .stream()
                .sorted((o1, o2) -> o1.getValue() > o2.getValue() ? -1 : 1)
                .forEach(entry -> {
                    result[0] += entry.getKey() + ": " + entry.getValue() + "\n";
                });
        return result[0];
    }

    private static void compareKnownPath(PlannerInitializer plannerInitializer, int count) {
        RoutePlanner perfectPlanner = plannerInitializer.perfectRoutePlanner;

        File odFile = new File(Storage.OD_PAIR_PATH + "pair_" + count + ".txt");
        Location[] odPair = SerializationUtils.readODPairFromGson(odFile);

        List<GraphEdge> perfectCarPath = perfectPlanner.findPath(odPair[0], odPair[1], TransportMode.CAR);
        List<GraphEdge> perfectTransitPath = perfectPlanner.findPath(odPair[0], odPair[1], TransportMode.TRANSIT);
        List<GraphEdge> perfectBikePath = perfectPlanner.findPath(odPair[0], odPair[1], TransportMode.BICYCLE);
        List<GraphEdge> perfectIntermodalPath = perfectPlanner.findPath(odPair[0], odPair[1]);

        if (perfectCarPath != null
                && perfectTransitPath != null
                && perfectBikePath != null
                && perfectIntermodalPath != null) {
            addToHistogram(perfectIntermodalPath
                    .stream()
                    .filter(graphEdge -> graphEdge.mode != TransportMode.WALK)
                    .collect(Collectors.toList())
            );
        } else return;

        Long carRef = perfectPlanner.getDuration(perfectCarPath);
        Long transitRef = perfectPlanner.getDuration(perfectTransitPath);
        Long interRef = perfectPlanner.getDuration(perfectIntermodalPath);
        Long bikeRef = perfectPlanner.getDuration(perfectBikePath);

        Storage.INTERMODAL_AVG_DURATION += interRef;

        if (carRef < transitRef && carRef < interRef && carRef < bikeRef) {
            Storage.CAR_PATH_COUNT++;
        } else if (interRef < transitRef && interRef < carRef && interRef < bikeRef) {
            Storage.INTERMODAL_PATH_COUNT++;
        } else if (bikeRef < transitRef && bikeRef < carRef && bikeRef < interRef) {
            Storage.BIKE_PATH_COUNT++;
        } else {
            Storage.TRANSIT_PATH_COUNT++;
        }
    }

    private static void comparePath(PlannerInitializer plannerInitializer, int count) {
        Location[] odPair = null;
        List<GraphEdge> perfectCarPath = null;
        List<GraphEdge> perfectTransitPath = null;
        List<GraphEdge> perfectBikePath = null;
        List<GraphEdge> perfectIntermodalPath = null;

        RoutePlanner perfectPlanner = plannerInitializer.perfectRoutePlanner;

        while (!ObjectUtils.allNotNull(perfectCarPath,
                perfectTransitPath,
                perfectBikePath,
                perfectIntermodalPath)) {
            odPair = Location.generateRandomLocationsInPrague(2);
            //CAR
            perfectCarPath = perfectPlanner.findPath(odPair[0], odPair[1], TransportMode.CAR);
//            List<GraphEdge> metaCarPath = metaPlanner.findPath(odPair[0], odPair[1], TransportMode.CAR);
            //TRANSIT
            perfectTransitPath = perfectPlanner.findPath(odPair[0], odPair[1], TransportMode.TRANSIT);
//            List<GraphEdge> metaTransitPath = metaPlanner.findPath(odPair[0], odPair[1], TransportMode.TRANSIT);

            //BIKE
            perfectBikePath = perfectPlanner.findPath(odPair[0], odPair[1], TransportMode.BICYCLE);
//            List<GraphEdge> metaBikePath = metaPlanner.findPath(odPair[0], odPair[1], TransportMode.BICYCLE);

            //INTERMODAL
            perfectIntermodalPath = perfectPlanner.findPath(odPair[0], odPair[1]);
//            List<GraphEdge> metaIntermodalPath = metaPlanner.findPath(odPair[0], odPair[1]);

//            if (ObjectUtils.allNotNull(perfectCarPath, metaCarPath,
//                    perfectTransitPath, metaTransitPath,
//                    perfectBikePath, metaBikePath,
//                    perfectIntermodalPath, metaIntermodalPath)) {
        }

        storeODPair(odPair, count);

        addToHistogram(perfectIntermodalPath
                .stream()
                .filter(graphEdge -> graphEdge.mode != TransportMode.WALK)
                .collect(Collectors.toList())
        );


//                Long carMeta = metaPlanner.getDuration(metaCarPath);
        Long carRef = perfectPlanner.getDuration(perfectCarPath);

//                Long transitMeta = metaPlanner.getDuration(metaTransitPath);
        Long transitRef = perfectPlanner.getDuration(perfectTransitPath);

//                Long interMeta = metaPlanner.getDuration(metaIntermodalPath);
        Long interRef = perfectPlanner.getDuration(perfectIntermodalPath);

//                Long bikeMeta = metaPlanner.getDuration(metaBikePath);
        Long bikeRef = perfectPlanner.getDuration(perfectBikePath);

//                String interDescriptionMeta = getIntermodalDescription(metaIntermodalPath);
//        String interDescriptionRef = getIntermodalDescription(perfectIntermodalPath);
                /*
                printWriter.println(count + ": "
                        + carMeta + ", " + carRef + ", "
                        + transitMeta + ", " + transitRef + ", "
                        + bikeMeta + ", " + bikeRef + ", "
                        + interMeta + ", " + interRef + ", "
                        + interDescriptionMeta + ", "
                        + interDescriptionRef
                );
                */
//        printWriter.println(count + ": " + carRef + ", " + transitRef + ", " + bikeRef
//                + ", " + interRef + ", " + interDescriptionRef
//        );
//        printWriter.println();

        if (interRef < transitRef && interRef < carRef && interRef < bikeRef) {
            Storage.INTERMODAL_PATH_COUNT++;
        } else if (carRef < transitRef && carRef < interRef && carRef < bikeRef) {
            Storage.CAR_PATH_COUNT++;
        } else if (bikeRef < transitRef && bikeRef < carRef && bikeRef < interRef) {
            Storage.BIKE_PATH_COUNT++;
        } else {
            Storage.TRANSIT_PATH_COUNT++;
        }
    }

    private static void storeODPair(Location[] pair, int count) {
        File file = new File(Storage.OD_PAIR_PATH + "pair_" + count + ".txt");

        SerializationUtils.writeRequestToGson(pair, file);
    }

    private static void addToHistogram(List<GraphEdge> path) {
        String keyDesc = getHistogramDescription(path);

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
                "/statistics_graph_requests" + Storage.TOTAL_REQUEST_COUNT + ".txt");
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
