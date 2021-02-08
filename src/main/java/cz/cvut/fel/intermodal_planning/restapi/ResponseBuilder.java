package cz.cvut.fel.intermodal_planning.restapi;

import cz.cvut.fel.intermodal_planning.planner.model.Leg;
import cz.cvut.fel.intermodal_planning.planner.model.Route;
import cz.cvut.fel.intermodal_planning.planner.model.TransportMode;
import cz.cvut.fel.intermodal_planning.general.utils.ColorUtils;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public class ResponseBuilder {

    public static String buildRouteDescription(Route path) {
        String description = "";
        if (path.isEmpty()) return description;

        long curDuration = path.legList.get(0).durationInSeconds;
        TransportMode curMode = path.legList.get(0).transportMode;
        Leg curLeg;
        description += "{\"legs\": [";
        for (int i = 1; i < path.legList.size(); i++) {
            curLeg = path.legList.get(i);
            if (curMode == curLeg.transportMode) {
                curDuration += curLeg.durationInSeconds;
            } else {
                description += "{";
                description += "\"duration\":" + curDuration + ",";
                description += "\"transportMode\":\"" + curMode + "\",";
                description += "\"color\": \"" + ColorUtils.toHexString(curMode.modeColor()) + "\"";
                description += "},";
                curMode = curLeg.transportMode;
                curDuration = curLeg.durationInSeconds;

            }
        }
        description += "{";
        description += "\"duration\":" + curDuration + ",";
        description += "\"transportMode\":\"" + curMode + "\",";
        description += "\"color\": \"" + ColorUtils.toHexString(curMode.modeColor()) + "\"";
        description += "}]}";
        return description;
    }
}
