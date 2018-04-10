package cz.cvut.fel.intermodal_planning.general;

import java.net.URL;

public final class Storage {
    public static long INTERMODAL_AVG_DURATION = 0;
    public static URL GRAPH_RESOURCE = Storage.class.getResource("/graph.json");
    public static URL METAGRAPH_RESOURCE = Storage.class.getResource("/metagraph.json");

    private static final String ROOT_PATH = "/Users/ondrejprenek/Documents/CVUT/Bachelor_thesis/Intermodal_planning/";

    public static String DATA_PATH = ROOT_PATH + "Data/";
    public static String STATISTICS_PATH = DATA_PATH + "statistics/";
    public static String REQUEST_PATH = DATA_PATH + "requests/";
    public static String OD_PAIR_PATH = DATA_PATH + "odpairs/";

    public static final String OTP_ENDPOINT = "http://127.0.0.1:8080/otp/routers/default/plan";
    public static final String GMAPS_REQUEST_STORAGE = REQUEST_PATH + "gmaps/";
    public static final String OTP_REQUEST_STORAGE = REQUEST_PATH + "otp/";

    public static String DESCRIPTION_HEADER = "count: car ref, transit ref, bike ref," +
            " intermodal ref,intermodal description ref" +
            "--------------------------------------------------------------------------------";

    public static String[] GMAPS_API_KEYS = new String[]{
            "AIzaSyDPlXXLGlWdjqyha8M5IWJMfgM4kf_uV4A",
            "AIzaSyBAJwyMaIhPdMptdXbDcFYGzl-86J2oyKw",
            "AIzaSyB1BXePutkWKhuKzk3TTTFKvjtaDKslI5A",
            "AIzaSyDqe-iuB8J9H4zUwZ1APCYzayuOx7-oKgg",
            "AIzaSyBdF2b3EpPazT5NPl_xRlcWXpBH4Pt0GWE",
            "AIzaSyDePcOSehCxz5SCFsDMPVqlUHq0BMIrTx4",
            "AIzaSyBsYq8wlQIlEHu0XpSWR-u1pbRJogKwdiQ",
            "AIzaSyDTcjYJ339Hah37BlxIJcAacKGlBwMwuhQ"
    };

    public static int CAR_REQUEST_COUNT = 10000;
    public static int TRANSIT_REQUEST_COUNT = 10000;
    public static int BIKE_REQUEST_COUNT = 10000;
    public static int WALK_REQUEST_COUNT = 10000;
    public static int TOTAL_REQUEST_COUNT = CAR_REQUEST_COUNT + TRANSIT_REQUEST_COUNT + BIKE_REQUEST_COUNT + WALK_REQUEST_COUNT;

    public static int INTERMODAL_PATH_COUNT = 0;
    public static int CAR_PATH_COUNT = 0;
    public static int TRANSIT_PATH_COUNT = 0;
    public static int BIKE_PATH_COUNT = 0;
    public static int WALK_PATH_COUNT = 0;
    public static int TOTAL_PATH_COUNT = CAR_PATH_COUNT + TRANSIT_PATH_COUNT + BIKE_PATH_COUNT + WALK_PATH_COUNT + INTERMODAL_PATH_COUNT;

    public static final float CAR_SPEED_MPS = 10;
    public static final float TRANSIT_SPEED_MPS = 12;
    public static final float BIKE_SPEED_MPS = 5.5f;
    public static final float WALK_SPEED_MPS = 1.4f;
}
