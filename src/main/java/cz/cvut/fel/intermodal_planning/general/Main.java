package cz.cvut.fel.intermodal_planning.general;

import cz.cvut.fel.intermodal_planning.planner.PlannerInitializer;
import cz.cvut.fel.intermodal_planning.planner.PlannerQualityEvaluator;
import cz.cvut.fel.intermodal_planning.planner.PlannerStatistics;
import cz.cvut.fel.intermodal_planning.planner.model.LocationArea;
import org.apache.log4j.BasicConfigurator;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public class Main {

    /**
     * Compare all strategies and compare abstraction quality created by them
     *
     * @param args
     */
    public static void main(String[] args) {
        PlannerQualityEvaluator.doExpansionStrategyComparision(Storage.AREA_PRAGUE);
    }
}
