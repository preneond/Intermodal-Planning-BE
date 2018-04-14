package cz.cvut.fel.intermodal_planning.general;

import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;
import cz.cvut.fel.intermodal_planning.planner.PlannerInitializer;
import cz.cvut.fel.intermodal_planning.planner.PlannerStatistics;
import cz.cvut.fel.intermodal_planning.utils.SerializationUtils;
import org.apache.log4j.BasicConfigurator;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class Main {
    static {
        BasicConfigurator.configure();
    }

    public static boolean EXTENDED = true;

    public static void main(String[] args) {
        PlannerInitializer plannerInitializer = new PlannerInitializer();
        PlannerStatistics.doComparision(plannerInitializer);

    }
}
