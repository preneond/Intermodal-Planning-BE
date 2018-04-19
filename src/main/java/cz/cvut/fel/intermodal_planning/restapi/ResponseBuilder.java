package cz.cvut.fel.intermodal_planning.restapi;

import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;
import cz.cvut.fel.intermodal_planning.utils.ColorUtils;

import java.util.List;

public class ResponseBuilder {

    public static String buildPathDescription(List<GraphEdge> path) {
        String description = "";
        if (path == null || path.isEmpty()) return description;

        long curDuration = path.get(0).durationInSeconds;
        TransportMode curMode = path.get(0).mode;
        GraphEdge curEdge;
        description += "{\"legs\": [";
        for (int i = 1; i < path.size(); i++) {
            curEdge = path.get(i);
            if (curMode == curEdge.mode) {
                curDuration += curEdge.durationInSeconds;
            } else {
                description += "{";
                description += "\"duration\":" + curDuration + ",";
                description += "\"mode\":\"" + curMode + "\",";
                description += "\"color\": \"" + ColorUtils.toHexString(curMode.modeColor()) + "\"";
                description += "},";
                curMode = curEdge.mode;
                curDuration = curEdge.durationInSeconds;

            }
        }
        description += "{";
        description += "\"duration\":" + curDuration + ",";
        description += "\"mode\":\"" + curMode + "\",";
        description += "\"color\": \"" + ColorUtils.toHexString(curMode.modeColor()) + "\"";
        description += "}]}";
        return description;
    }
}
