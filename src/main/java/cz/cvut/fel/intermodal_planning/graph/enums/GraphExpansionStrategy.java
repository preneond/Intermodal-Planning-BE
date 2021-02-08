package cz.cvut.fel.intermodal_planning.graph.enums;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public enum GraphExpansionStrategy {
    //Uninformed strategies
    RANDOM_OD, RANDOM_OD_WITH_MIN_DISTANCE_BETWEEN, CHAINING_RANDOM_OD,

    //Informed strategies
    NODES_MIN_COVERAGE_UNIF_DIST, NODES_MIN_COVERAGE_NORM_DIST,
    EDGES_MIN_COVERAGE_UNIF_DIST, EDGES_MIN_COVERAGE_NORM_DIST,

    //Supervised strategies
    USING_KNOWN_NODES_AS_OD;

    /**
     * Getter for uninformed strategies
     *
     * @return uninformed strategies
     */
    public static GraphExpansionStrategy[] getUninformedStrategies() {
        return new GraphExpansionStrategy[]{RANDOM_OD_WITH_MIN_DISTANCE_BETWEEN, CHAINING_RANDOM_OD};
    }

    /**
     * Getter for informed strategies
     *
     * @return informed strategies
     */
    public static GraphExpansionStrategy[] getInformedStrategies() {
        return new GraphExpansionStrategy[]{NODES_MIN_COVERAGE_UNIF_DIST, NODES_MIN_COVERAGE_NORM_DIST,
                EDGES_MIN_COVERAGE_UNIF_DIST, EDGES_MIN_COVERAGE_NORM_DIST};
    }

    /**
     * Getter for supervised strategies
     *
     * @return supervised strategies
     */
    public static GraphExpansionStrategy[] getSupervisedStrategies() {
        return new GraphExpansionStrategy[]{USING_KNOWN_NODES_AS_OD};
    }

}
