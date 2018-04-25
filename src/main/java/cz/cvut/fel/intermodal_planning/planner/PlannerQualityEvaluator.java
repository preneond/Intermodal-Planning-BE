package cz.cvut.fel.intermodal_planning.planner;

import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.graph.enums.GraphQualityMetric;
import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.Location;
import cz.cvut.fel.intermodal_planning.model.planner.LocationArea;
import cz.cvut.fel.intermodal_planning.model.planner.Route;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PlannerQualityEvaluator {
    private static final Logger logger = LogManager.getLogger(PlannerInitializer.class);

    static void evaluatePlannerQuality(PlannerInitializer plannerInitializer, GraphQualityMetric qualityMetric) {
        logger.info("Checking planner using " + plannerInitializer.expansionStrategy.name() + " strategy by " + qualityMetric.name() + "quality metrix");
        switch (qualityMetric) {
            case REFINEMENT:
                evaluatePlannerQualityUsingRefinement(plannerInitializer);
                break;
            case SINGLEMODAL:
                evaluatePlannerQualityUsingSinglemodalQualityCheck(plannerInitializer);
                break;
        }
    }

    private static void evaluatePlannerQualityUsingSinglemodalQualityCheck(PlannerInitializer plannerInitializer) {
        int findingPathCount = 100;
        long[] routeDuration, subplannerRouteDuration, deviation;
        double[] modeDeviation = new double[TransportMode.availableModes().length];
        File file = new File(Storage.STATISTICS_PATH + "/planner_quality_singlemodal.txt");

        try {
            FileWriter writer = new FileWriter(file, true);

            int modeCount =0;
            for (TransportMode transportMode : TransportMode.availableModes()) {
                routeDuration = new long[findingPathCount];
                subplannerRouteDuration = new long[findingPathCount];
                deviation = new long[findingPathCount];

                for (int i = 0; i < findingPathCount; i++) {
                    List<GraphEdge> plannerRoute = null;
                    Route subplannerRoute = null;

                    while (!ObjectUtils.allNotNull(plannerRoute, subplannerRoute)) {
                        Location[] locArray = plannerInitializer.locationArea.generateRandomLocations(2);
                        plannerRoute = plannerInitializer.routePlanner.findPath(locArray[0], locArray[1], transportMode);
                        subplannerRoute = plannerInitializer.routePlanner.findRouteBySubplanner(locArray[0], locArray[1], transportMode);
                    }

                    routeDuration[i] = plannerInitializer.routePlanner.getDuration(plannerRoute);
                    subplannerRouteDuration[i] = plannerInitializer.routePlanner.getDuration(subplannerRoute);
                    deviation[i] = routeDuration[i] - subplannerRouteDuration[i];
                }
                modeDeviation[modeCount++] = Arrays.stream(deviation).average().orElse(0);

            }

            double avgDeviation = Arrays.stream(modeDeviation).average().orElse(0);

            writer.write(plannerInitializer.requestCount + " requests, " +
                    "strategy: " + plannerInitializer.expansionStrategy.name() + "," +
                    "avg deviation: " + avgDeviation + " s\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void evaluatePlannerQualityUsingRefinement(PlannerInitializer plannerInitializer) {
        double sizeDeviation;
        int findingPathCount = 100;

        long[] routeDuration = new long[findingPathCount];
        long[] refinementRouteDuration = new long[findingPathCount];
        long[] deviation = new long[findingPathCount];

        LocationArea locationArea = plannerInitializer.locationArea;

        for (int i = 0; i < findingPathCount; i++) {
            List<GraphEdge> graphPath = null;

            while (graphPath == null) {
                graphPath = plannerInitializer.routePlanner.findRandomPath(locationArea);
            }

            routeDuration[i] = plannerInitializer.routePlanner.getDuration(graphPath);
            Route refinementRoute = plannerInitializer.routePlanner.doRefinement(graphPath);
            refinementRouteDuration[i] = plannerInitializer.routePlanner.getDuration(refinementRoute);
            deviation[i] = routeDuration[i] - refinementRouteDuration[i];
        }

        sizeDeviation = Arrays.stream(deviation).average().orElse(0);


        File file = new File(Storage.STATISTICS_PATH + "/planner_quality_refinement.txt");
        try {
            FileWriter writer = new FileWriter(file, true);
            writer.write(plannerInitializer.requestCount + "requests," +
                    "strategy: " + plannerInitializer.expansionStrategy.name() + "," +
                    "avg deviation: " + sizeDeviation + " s\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
