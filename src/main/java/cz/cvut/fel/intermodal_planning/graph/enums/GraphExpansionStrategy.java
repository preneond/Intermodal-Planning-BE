package cz.cvut.fel.intermodal_planning.graph.enums;

public enum GraphExpansionStrategy {
    //Uninformed strategies
    RANDOM_OD, RANDOM_OD_WITH_MIN_DISTANCE_BETWEEN, CHAINING_RANDOM_OD,

    //Informed strategies
    NODES_MIN_COVERAGE_EQ_DIST, NODES_MIN_COVERAGE_NORM_DIST,
    EDGES_MIN_COVERAGE_EQ_DIST, EDGES_MIN_COVERAGE_NORM_DIST,

    //Supervised strategies
    USING_KNOWN_NODES_AS_OD;


    public static GraphExpansionStrategy[] getInformedStrategies() {
        return new GraphExpansionStrategy[]{RANDOM_OD, RANDOM_OD_WITH_MIN_DISTANCE_BETWEEN, CHAINING_RANDOM_OD};
    }

    public static GraphExpansionStrategy[] getUninformedStrategies() {
        return new GraphExpansionStrategy[]{NODES_MIN_COVERAGE_EQ_DIST, NODES_MIN_COVERAGE_NORM_DIST,
                EDGES_MIN_COVERAGE_EQ_DIST, EDGES_MIN_COVERAGE_NORM_DIST};
    }

    public static GraphExpansionStrategy[] getSupervisedStrategies() {
        return new GraphExpansionStrategy[]{USING_KNOWN_NODES_AS_OD};
    }

}
