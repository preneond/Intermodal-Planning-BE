package cz.cvut.fel.intermodal_planning.general;

import cz.cvut.fel.intermodal_planning.planner.PlannerInitializer;
import cz.cvut.fel.intermodal_planning.planner.PlannerStatistics;
import org.apache.log4j.BasicConfigurator;

public class Main {
    static {
        BasicConfigurator.configure();
    }

    public static void main(String[] args) {
        PlannerInitializer plannerInitializer = new PlannerInitializer();
        PlannerStatistics.doComparision(plannerInitializer);

    }


}