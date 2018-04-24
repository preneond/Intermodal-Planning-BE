package cz.cvut.fel.intermodal_planning.planner;

import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.graph.enums.GraphQualityMetric;
import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.LocationArea;
import cz.cvut.fel.intermodal_planning.model.planner.Route;
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
        logger.info("Checking planner quality using " + plannerInitializer.expansionStrategy);
        switch (qualityMetric) {
            case REFINEMENT:
                evaluatePlannerQualityUsingRefinement(plannerInitializer);
            case SINGLEMODAL:
                evaluatePlannerQualityUsingSinglemodalQualityCheck(plannerInitializer);
        }
    }

    private static void evaluatePlannerQualityUsingSinglemodalQualityCheck(PlannerInitializer plannerInitializer) {

    }

    private static void evaluatePlannerQualityUsingRefinement(PlannerInitializer plannerInitializer) {
        double sizeDeviation;
        int findingPathCount = 100;

        long[] routeDuration = new long[findingPathCount];
        long[] refinementRouteDuration = new long[findingPathCount];
        long[] deviation = new long[findingPathCount];

        LocationArea locationArea = plannerInitializer.locationArea;

        for (int j = 0; j < findingPathCount; j++) {
            List<GraphEdge> graphPath = plannerInitializer.routePlanner.findRandomPath(locationArea);
            routeDuration[j] = plannerInitializer.routePlanner.getDuration(graphPath);
            Route refinementRoute = plannerInitializer.routePlanner.doRefinement(graphPath);
            refinementRouteDuration[j] = plannerInitializer.routePlanner.getDuration(refinementRoute);
            deviation[j] = routeDuration[j] - refinementRouteDuration[j];
        }

        sizeDeviation = Arrays.stream(deviation).average().orElse(0);

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
