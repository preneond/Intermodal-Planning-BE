package cz.cvut.fel.intermodal_planning.planner;

import cz.cvut.fel.intermodal_planning.general.Constants;
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

    public static void doComparision(GraphMaker perfectGraphMaker, GraphMaker metaGraphMaker) {
        RoutePlanner perfectPlanner = new RoutePlanner(perfectGraphMaker);
        RoutePlanner metaPlanner = new RoutePlanner(metaGraphMaker);

        File file = new File(Constants.STATISTICS_PATH + "/comparision.txt");
        try {
            PrintWriter printWriter = new PrintWriter(file);
//            printWriter.println("count: car meta, car ref, transit meta, transit ref, bike meta, bike ref, intermodal meta, intermodal ref, intermodal description meta, intermodal description ref");
//            printWriter.println("---------------------------------------------------------------------------------------------------------------------------------------------");


            printWriter.println(Constants.DESCRIPTION_HEADER);

            histogramMap = new HashMap<>();

            for (int i = 0; i < 1000; i++) {
                comparePath(perfectPlanner, metaPlanner, printWriter, i + 1);
            }

            SerializationUtils.writeObjectToFile(stringify(histogramMap),
                    new File(Constants.STATISTICS_PATH + "/histogram.txt"));

            printWriter.close();

            logger.info("Car count: " + Constants.CAR_PATH_COUNT);
            logger.info("Transit count: " + Constants.TRANSIT_PATH_COUNT);
            logger.info("Bike count: " + Constants.BIKE_PATH_COUNT);
            logger.info("Intermodal count: " + Constants.INTERMODAL_PATH_COUNT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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

    private static void comparePath(RoutePlanner perfectPlanner, RoutePlanner metaPlanner, PrintWriter printWriter, int count) {
        Location[] odPair;
        List<GraphEdge> perfectCarPath = null;
        List<GraphEdge> perfectTransitPath = null;
        List<GraphEdge> perfectBikePath = null;
        List<GraphEdge> perfectIntermodalPath = null;

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
        String interDescriptionRef = getIntermodalDescription(perfectIntermodalPath);
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
        printWriter.println(count + ": " + carRef + ", " + transitRef + ", " + bikeRef
                + ", " + interRef + ", " + interDescriptionRef
        );
        printWriter.println();

        if (carRef < transitRef && carRef < interRef) {//&& carRef < bikeRef) {
            Constants.CAR_PATH_COUNT++;
        } else if (interRef < transitRef && interRef < carRef) {//&& interRef < bikeRef) {
            Constants.INTERMODAL_PATH_COUNT++;
        } else if (bikeRef < transitRef && bikeRef < carRef && bikeRef < interRef) {
            Constants.BIKE_PATH_COUNT++;
        } else {
            Constants.TRANSIT_PATH_COUNT++;
        }
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

//        int[] graphSize = new int[]{85};
        double sizeDeviation;

        int findingPathCount = 100;

//        for (int i = 0; i < graphSize.length; i++) {
        long[] routeDuration = new long[findingPathCount];
        long[] refinementRouteDuration = new long[findingPathCount];
        long[] deviation = new long[findingPathCount];
//            GraphMaker.getInstance().createGraph(routePlanner.expandGraph(graphSize[i]));

        for (int j = 0; j < findingPathCount; j++) {
            List<GraphEdge> graphPath = routePlanner.findRandomPath();
            routeDuration[j] = routePlanner.getDuration(graphPath);

            Route refinementRoute = routePlanner.doRefinement(graphPath);
            refinementRouteDuration[j] = routePlanner.getDuration(refinementRoute);

            deviation[j] = routeDuration[j] - refinementRouteDuration[j];
        }

        sizeDeviation = Arrays.stream(deviation).average().orElse(0);

        File file = new File(Constants.STATISTICS_PATH + "/statistics_graph_requests" + 10000 + ".txt");
        try {
            FileWriter writer = new FileWriter(file, false);
            for (int ii = 0; ii < findingPathCount; ii++) {
                writer.write(routeDuration[ii] + " " + refinementRouteDuration[ii] + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File reqfile = new File(Constants.STATISTICS_PATH + "/requests.txt");
        try {
            FileWriter writer = new FileWriter(reqfile, true);
//            for (int k = 0; k < graphSize.length; k++) {
            writer.write(10000 + " requests: " + sizeDeviation + "\n");
//            }
            writer.close();
        } catch (
                IOException e)

        {
            e.printStackTrace();
        }

    }
}
