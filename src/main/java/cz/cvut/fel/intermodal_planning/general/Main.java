package cz.cvut.fel.intermodal_planning.general;

import cz.cvut.fel.intermodal_planning.graph.GraphMaker;
import cz.cvut.fel.intermodal_planning.graph.enums.GraphQualityMetric;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;
import cz.cvut.fel.intermodal_planning.planner.PlannerInitializer;
import cz.cvut.fel.intermodal_planning.planner.PlannerQualityEvaluator;
import cz.cvut.fel.intermodal_planning.planner.PlannerStatistics;
import org.apache.log4j.BasicConfigurator;

public class Main {
    static {
        BasicConfigurator.configure();
    }

    public static void main(String[] args) {
//        PlannerStatistics.doExpansionStrategyComparision(Storage.AREA_PRAGUE);
//        PlannerQualityEvaluator.compareNormalRefinementPaths(PlannerInitializer.getInstance());
//        PlannerQualityEvaluator.evaluatePlannerQuality(PlannerInitializer.getInstance(), GraphQualityMetric.REFINEMENT);

    }
}
