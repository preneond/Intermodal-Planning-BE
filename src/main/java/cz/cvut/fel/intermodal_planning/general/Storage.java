package cz.cvut.fel.intermodal_planning.general;

import cz.cvut.fel.intermodal_planning.planner.model.Location;
import cz.cvut.fel.intermodal_planning.planner.model.LocationArea;

import java.net.URL;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public final class Storage {
    public static URL GRAPH_RESOURCE = Storage.class.getResource("/graph.json");

    private static final String ROOT_PATH = "/path/to/root";

    public static String DATA_PATH = ROOT_PATH + "Data/";
    public static String STATISTICS_PATH = DATA_PATH + "statistics/";
    public static String REQUEST_PATH = DATA_PATH + "requests/";
    public static String OD_PAIR_PATH = DATA_PATH + "odpairs/";

    public static final String OTP_ENDPOINT = "****";
    public static final String GMAPS_REQUEST_STORAGE = REQUEST_PATH + "gmaps/";
    public static final String OTP_REQUEST_STORAGE = REQUEST_PATH + "otp/";

    public static long INTERMODAL_AVG_DURATION = 0;


    /**
     * Minimal Distance between OD pair used in the uninformed strategy
     */
    public static final int MIN_DISTANCE_IN_METERS_BETWEEN_OD = 500;


    /**
     * Request counters
     */
    public static int CAR_REQUEST_COUNT = 0;
    public static int TRANSIT_REQUEST_COUNT = 0;
    public static int BIKE_REQUEST_COUNT = 0;
    public static int WALK_REQUEST_COUNT = 0;

    /**
     * Speed of vehicles used for determining graph's edge duration
     */
    public static final float CAR_SPEED_MPS = 10;
    public static final float TRANSIT_SPEED_MPS = 10;
    public static final float BIKE_SPEED_MPS = 4.16f;
    public static final float WALK_SPEED_MPS = 1.4f;


    /**
     *  num of cells for distribution grid for informed strategies
     */
    public static final int GRAPH_DISTRIBUTION_GRID_X = 30;
    public static final int GRAPH_DISTRIBUTION_GRID_Y = 30;

    public static int KNOWN_REQUEST_COUNT = 20000;

    public static final int FINDING_PATH_COUNT = 100;


    /**
     * Selected test region - Prague
     */
    public static final LocationArea AREA_PRAGUE = new LocationArea(
            50.1072,
            50.0269,
            14.2946,
            14.55);


    /**
     * Important Places for Supervised Strategies
     */
    public static final Location[] IMPORTANT_PLACES_PRAGUE = new Location[]{
            // Kulatak
            new Location(50.100174, 14.39562),
            // Vaclavak
            new Location(50.081747, 14.427189),
            // Karlak
            new Location(50.075739, 14.420056),
            // Cerny most
            new Location(50.106449, 14.574448),
            // Jizni Spojka
            new Location(50.051460, 14.478884),
            //IP
            new Location(50.075358, 14.4299780),
            //Florenc
            new Location(50.091170, 14.438711)
    };

    /**
     * Auth Keys for Google Maps API
     */
    public static String[] GMAPS_API_KEYS = new String[]{
           "****"
    };
}
