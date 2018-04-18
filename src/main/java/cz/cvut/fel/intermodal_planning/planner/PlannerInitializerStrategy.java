package cz.cvut.fel.intermodal_planning.planner;

public abstract class PlannerInitializerStrategy {

    public void expandGraph() {
        expandByRandomOD();
        expandByChainingOfRandomOD();
        expandByRandomODWithMinDistance(100);
        expandBySearchingNodesEqualDist();
        expandBySearchingEdgesNormDist();
    }

    private void expandBySearchingNodesEqualDist() {

    }

    private void expandBySearchingEdgesNormDist() {
    }

    private void expandByRandomOD() {

    }

    private void expandByChainingOfRandomOD() {

    }

    protected void expandByRandomODWithMinDistance(int distanceInMeters) {

    }



}
