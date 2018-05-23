package cz.cvut.fel.intermodal_planning.planner;

import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.graph.enums.GraphExpansionStrategy;
import cz.cvut.fel.intermodal_planning.graph.enums.GraphQualityMetric;
import cz.cvut.fel.intermodal_planning.planner.model.Location;
import cz.cvut.fel.intermodal_planning.planner.model.LocationArea;
import cz.cvut.fel.intermodal_planning.planner.model.Route;
import cz.cvut.fel.intermodal_planning.planner.model.TransportMode;
import cz.cvut.fel.intermodal_planning.general.utils.GeoJSONBuilder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public class PlannerQualityEvaluator {
    private static final Logger logger = LogManager.getLogger(PlannerInitializer.class);


    /**
     * Comparison of Picking OD Pair Strategies
     *
     * @param area Selected Test Region
     */
    public static void doExpansionStrategyComparision(LocationArea area) {
        PlannerInitializer plannerInitializer;
        int[] requestCountArr = new int[]{500, 1000, 2500, 5000, 7500, 10000};

        for (GraphExpansionStrategy expansionStrategy : GraphExpansionStrategy.getUninformedStrategies()) {
            plannerInitializer = new PlannerInitializer(area);
            for (int requestCount: requestCountArr) {
                RoutePlanner routePlanner = plannerInitializer.initPlanner(requestCount,expansionStrategy);
                evaluatePlannerQualityUsingRefinement(routePlanner,area,expansionStrategy,requestCount);
            }
        }
    }

    /**
     * Evaluation of abstraction quality
     *
     * @param plannerInitializer PlannerInitializer
     * @param qualityMetric Selected Abstraction quality metric
     */
    public static void evaluatePlannerQuality(PlannerInitializer plannerInitializer, GraphQualityMetric qualityMetric) {
        logger.info("Checking planner using " + plannerInitializer.expansionStrategy.name()
                + " strategy by " + qualityMetric.name() + "quality metrix");
        switch (qualityMetric) {
            case REFINEMENT:
                evaluatePlannerQualityUsingRefinement(plannerInitializer.routePlanner,
                        plannerInitializer.locationArea,
                        plannerInitializer.expansionStrategy,
                        plannerInitializer.requestCount);
                break;
            case SINGLEMODAL:
                evaluatePlannerQualityUsingSinglemodalQualityCheck(plannerInitializer);
                break;
        }
    }

    /**
     * Evaluation of abstraction quality by checking Singlemodal Quality
     *
     * @param plannerInitializer PlannerInitializer instance
     */
    private static void evaluatePlannerQualityUsingSinglemodalQualityCheck(PlannerInitializer plannerInitializer) {
        int findingPathCount = Storage.FINDING_PATH_COUNT;
        long[] routeDuration, subplannerRouteDuration, deviation;
        double[] modeDeviation = new double[TransportMode.availableModes().length];
        File file = new File(Storage.STATISTICS_PATH + "/planner_quality_singlemodal.txt");

        try {
            FileWriter writer = new FileWriter(file, true);

            int modeCount = 0;
            for (TransportMode transportMode : TransportMode.singleModalModes()) {
                routeDuration = new long[findingPathCount];
                subplannerRouteDuration = new long[findingPathCount];
                deviation = new long[findingPathCount];

                for (int i = 0; i < findingPathCount; i++) {
                    Location[] locArray = plannerInitializer.locationArea.generateRandomLocations(2);
                    Route plannerRoute = plannerInitializer.routePlanner.metasearchRoute(locArray[0], locArray[1], transportMode);
                    Route subplannerRoute = plannerInitializer.routePlanner.searchRouteUsingSubplanner(locArray[0], locArray[1], transportMode);

                    routeDuration[i] = plannerInitializer.routePlanner.getRouteDuration(plannerRoute);
                    subplannerRouteDuration[i] = plannerInitializer.routePlanner.getRouteDuration(subplannerRoute);
                    deviation[i] = routeDuration[i] - subplannerRouteDuration[i];
                }
                modeDeviation[modeCount++] = Arrays.stream(deviation).average().orElse(0);

            }

            double avgDeviation = Arrays.stream(modeDeviation).sum();

            writer.write(plannerInitializer.requestCount + " requests, " +
                    "strategy: " + plannerInitializer.expansionStrategy.name() + "," +
                    "deviation sum: " + avgDeviation + " s\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Evaluation of abstraction quality using refinement
     *
     * @param routePlanner RoutePlanner Instance
     * @param locationArea Selected Test Area
     * @param expansionStrategy Picking OD Pairs strategy
     * @param requestCount Number of Requests
     */
    public static void evaluatePlannerQualityUsingRefinement(RoutePlanner routePlanner, LocationArea locationArea,
                                                             GraphExpansionStrategy expansionStrategy, int requestCount) {
        double sizeDeviation;
        int findingPathCount = Storage.FINDING_PATH_COUNT;

        long[] routeDuration = new long[findingPathCount];
        long[] refinementRouteDuration = new long[findingPathCount];
        long[] deviation = new long[findingPathCount];

        for (int i = 0; i < findingPathCount; i++) {
            Route graphPath = routePlanner.metasearchRandomRoute(locationArea);
            Route refinementRoute = routePlanner.doRefinement(graphPath);

            routeDuration[i] = routePlanner.getRouteDuration(graphPath);
            refinementRouteDuration[i] = routePlanner.getRouteDuration(refinementRoute);
            deviation[i] = routeDuration[i] - refinementRouteDuration[i];
        }

        sizeDeviation = Arrays.stream(deviation).sum();

        File file = new File(Storage.STATISTICS_PATH + "/planner_quality_refinement.txt");
        try {
            FileWriter writer = new FileWriter(file, true);
            writer.write(requestCount + "requests," +
                    "strategy: " + expansionStrategy.name() + "," +
                    "deviation sum: " + sizeDeviation + " s\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Comparison of path before and after refinement
     *
     * @param plannerInitializer PlannerInitializer Instance
     */
    public static void compareNormalRefinementPaths(PlannerInitializer plannerInitializer) {
        RoutePlanner routePlanner = plannerInitializer.routePlanner;

        Route route = routePlanner.metasearchRandomRoute(plannerInitializer.locationArea);
        Route refoundedRoute = routePlanner.doRefinement(route);

        logger.info("Route duration: " + routePlanner.getRouteDuration(route));
        logger.info("Refounded route duration: " + routePlanner.getRouteDuration(refoundedRoute));

        System.out.println(GeoJSONBuilder.getInstance().buildGeoJSONStringForRoute(route));
        System.out.println(GeoJSONBuilder.getInstance().buildGeoJSONStringForRoute(refoundedRoute));
    }
}
