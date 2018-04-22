package cz.cvut.fel.intermodal_planning.general;

import cz.cvut.fel.intermodal_planning.graph.enums.GraphExpansionStrategy;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;
import cz.cvut.fel.intermodal_planning.planner.PlannerInitializer;
import cz.cvut.fel.intermodal_planning.planner.PlannerStatistics;
import cz.cvut.fel.intermodal_planning.utils.SerializationUtils;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.log4j.BasicConfigurator;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {
    static {
        BasicConfigurator.configure();
    }

    public static void main(String[] args) {
//        PlannerInitializer plannerInitializer = PlannerInitializer.getInstance();
//        PlannerStatistics.doComparision(plannerInitializer);
//        PlannerInitializer plannerInitializer = new PlannerInitializer(GraphExpansionStrategy.RANDOM_OD, Storage.AREA_PRAGUE);

        int gridRowsCount = 30;
        int gridColumnsCount = 30;
        int numOfNodes = 50000;

        int[][] gridNormDistribution = new int[gridRowsCount][gridColumnsCount];

        double[] means = new double[]{0, 0};
        double[][] covariances = new double[][]{{1, 0}, {0, 1}};
        MultivariateNormalDistribution distribution = new MultivariateNormalDistribution(means, covariances);

        double rowStepSize = 4 / (double) gridRowsCount;
        double columnStepSize = 4 / (double) gridColumnsCount;


        double val_j;
        double val_i = -2;
        for (int i = 0; i < gridRowsCount; i++, val_i += rowStepSize) {
            val_j = -2;
            for (int j = 0; j < gridColumnsCount; j++, val_j += columnStepSize) {
                gridNormDistribution[i][j] = (int) (numOfNodes * distribution.density(new double[]{val_i, val_j}));
                System.out.println(gridNormDistribution[i][j]);
            }
        }
    }
}
