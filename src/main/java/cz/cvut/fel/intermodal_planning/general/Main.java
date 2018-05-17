package cz.cvut.fel.intermodal_planning.general;

import cz.cvut.fel.intermodal_planning.planner.PlannerInitializer;
import org.apache.log4j.BasicConfigurator;

public class Main {
    static {
        BasicConfigurator.configure();
    }

    public static void main(String[] args) {
        PlannerInitializer.getInstance();
//        PlannerStatistics.doExpansionStrategyComparision(Storage.AREA_PRAGUE);
//        PlannerQualityEvaluator.compareNormalRefinementPaths(PlannerInitializer.getInstance());
//        PlannerQualityEvaluator.evaluatePlannerQuality(PlannerInitializer.getInstance(), GraphQualityMetric.REFINEMENT);

    }
}
