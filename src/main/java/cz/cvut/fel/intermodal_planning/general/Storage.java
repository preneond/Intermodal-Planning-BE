package cz.cvut.fel.intermodal_planning.general;

import cz.cvut.fel.intermodal_planning.model.planner.Location;
import cz.cvut.fel.intermodal_planning.model.planner.LocationArea;

import java.net.URL;

public final class Storage {
    public static URL GRAPH_RESOURCE = Storage.class.getResource("/graph.json");

    private static final String ROOT_PATH = "/Users/ondrejprenek/Documents/CVUT/Bachelor_thesis/Intermodal_planning/";

    public static String DATA_PATH = ROOT_PATH + "Data/";
    public static String STATISTICS_PATH = DATA_PATH + "statistics/";
    public static String REQUEST_PATH = DATA_PATH + "requests/";
    public static String OD_PAIR_PATH = DATA_PATH + "odpairs/";

    public static final String OTP_ENDPOINT = "http://127.0.0.1:8080/otp/routers/default/plan";
    public static final String GMAPS_REQUEST_STORAGE = REQUEST_PATH + "gmaps/";
    public static final String OTP_REQUEST_STORAGE = REQUEST_PATH + "otp/";

    public static long INTERMODAL_AVG_DURATION = 0;

    public static final int MIN_DISTANCE_IN_METERS_BETWEEN_OD = 500;

    public static int CAR_REQUEST_COUNT = 0;
    public static int TRANSIT_REQUEST_COUNT = 0;
    public static int BIKE_REQUEST_COUNT = 0;
    public static int WALK_REQUEST_COUNT = 0;

    public static final float CAR_SPEED_MPS = 10;
    public static final float TRANSIT_SPEED_MPS = 10;
    public static final float BIKE_SPEED_MPS = 4.16f;
    public static final float WALK_SPEED_MPS = 1.4f;

    public static final int GRAPH_DISTRIBUTION_GRID_X = 30;
    public static final int GRAPH_DISTRIBUTION_GRID_Y = 30;
    
    // In meters
    public static final int MIN_DISTANCE_BETWEEN_OD = 500;

    public static final LocationArea AREA_PRAGUE = new LocationArea(
            50.1072,
            50.0269,
            14.2946,
            14.55);

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
            "AIzaSyDTcjYJ339Hah37BlxIJcAacKGlBwMwuhQ",
            "AIzaSyDZXEPEEpBf6dIP1mObKAfemBODucsUDzQ",
            "AIzaSyDbn9PkZ1UHDzi-P1h57-mo-endxen87xU",

            "AIzaSyCZV9E5HbNR4CjrlEp_jMOJURL2BicG_YA",
            "AIzaSyDyPrdAcyz3LDVizBf8jquz1Vin9Y4Z9EI",
            "AIzaSyAXNikg6MUwdMpyYnv262tFPe_24HDpa2c",
            "AIzaSyDcfkhEBRsksDgV6wXMaEuo5JcamTiC4vE",
            "AIzaSyAcyZTi6cZhzPBbDrYQvkSHrxcmb1xmGic",
            "AIzaSyBjzfYZnQ-06T0PvBOyQciJ_T4V8_N_fVM",
            "AIzaSyB9q1BRhBx1bGvIWUU8XS9O3084pfcKT0U",
            "AIzaSyCIFkOUztayI2WM2-MW0-BHz29UWx5nImA",
            "AIzaSyDDWhg7lvEcbVh-wBkdQiPLeItnh8pEkbI",
            "AIzaSyBXRDozUF7nHfYmsNLhp67XqxFQbq4aJHQ",
            "AIzaSyCZPYKlhHT5FDxD865JM52oM2CC1Q_x-i4",

            "AIzaSyCGozoXcxaqvftw2uMIEEfrvLnGmvejV_g",
            "AIzaSyDc3e6AqFuoLo3CFJlTmd9Uh13gJffyoJY",
            "AIzaSyDy2NG8bbS9XWohxVO_3BCCnqxmb7cwzwg",
            "AIzaSyCr0-ICL2e7wiKme8NZBGN1PlN2-X5YMxk",
            "AIzaSyCx24szrOOBrY5MOCPV5_riS9ormoLvVSM",
            "AIzaSyBIOcxaLyVSn5yA8FjEusHst8ig1Ql6Kf8",
            "AIzaSyCPnugKTNY-RgXInnuSU2qM7b3GUUuKGVU",
            "AIzaSyDQyn032gKwSJrk94Xh7GUXlcKhBDrYCu8",
            "AIzaSyDnydUcFe15dC8iuKGL9RsRG39gNWQnsh0",
            "AIzaSyBnr2a2o2wGe0GDrxpkJ4Vs893I6oejjNM",
            "AIzaSyCIdROtkPV7yfR9diMsbr0XlSMVaMNTYzU"
    };


    public static void nullRequestCounters() {
        CAR_REQUEST_COUNT = 0;
        TRANSIT_REQUEST_COUNT = 0;
        BIKE_REQUEST_COUNT = 0;
        WALK_REQUEST_COUNT = 0;
    }

    private Storage() {
    }

    public static int getTotalRequestCount() {
        return CAR_REQUEST_COUNT + TRANSIT_REQUEST_COUNT + BIKE_REQUEST_COUNT + WALK_REQUEST_COUNT;
    }
}
