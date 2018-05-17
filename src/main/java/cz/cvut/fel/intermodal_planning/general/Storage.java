package cz.cvut.fel.intermodal_planning.general;

import cz.cvut.fel.intermodal_planning.planner.model.Location;
import cz.cvut.fel.intermodal_planning.planner.model.LocationArea;

import java.net.URL;

public final class Storage {
    public static URL GRAPH_RESOURCE = Storage.class.getResource("/graph.json");

    private static final String ROOT_PATH = "/Users/ondrejprenek/Documents/CVUT/Bachelor_thesis/source/";

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
//            50.1472,
//            50.0069,
//            14.2946,
//            14.5898);
//
//    public static final LocationArea AREA_PRAGUE_EXT = new LocationArea(
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
            "AIzaSyCIdROtkPV7yfR9diMsbr0XlSMVaMNTYzU",

            "AIzaSyAUgv7J0Zh0kYi6jLVhU_EMBbk0BaSUPsc",
            "AIzaSyCerD_3Kps7awYuvXbgJ4bQ6BmBTuH83bo",
            "AIzaSyDuKzxMVJc6wVhiMCVphzpEmwOyjM9eH3A",
            "AIzaSyCBlVhbXU05RSHqB3SQx8keCvBsK_pYnzA",
            "AIzaSyCvLXUi0EmhgydOR-bGUKdbrBS7CUsvR-k",
            "AIzaSyDj8PJmYGCUMz2Drydw5-FxIiSJaz7Ey-o",
            "AIzaSyBphBA-fk7lLBUjovENUrSk2iIuJInCw8Y",
            "AIzaSyD4RswjmwCvxc4K9rzG06YhbOCpPPAoYPw",
            "AIzaSyACgTqrn7Ex-5-za7MJdxxbrvBviFkDWH4",
            "AIzaSyDIJd9oDd_PWo2zJy5uPbtv8K7JtbIJ_1E",

            "AIzaSyAhijbAS1x_34zlSRROCUsyBn_HnwKgdC8",
            "AIzaSyBuGjuqsTJPrm8nVt0qmkeCV1jKTDCW8rE",
            "AIzaSyC5ITnvkeAywyfBVzrV842g3k7JQnqVj24",
            "AIzaSyDwRmiuYaiHAW2Bh7JW7GbOFN7bHUErrN8",
            "AIzaSyCSOr2ZzJad2qs61ANnYMZfVVMguoQdQhA",
            "AIzaSyDneayG-UaftJPtg7oDGIUPh-b20o1sg3Q",
            "AIzaSyBiA3aRZtosp3Aecz0hmc1LQ9kRxKliXRE",
            "AIzaSyDjc9PkcUpLDzn42VoTwIfnhnVx3a06zcI",
            "AIzaSyARFWGY5lB_yjVBKjEoUulOotVpu2Zp2oY",
            "AIzaSyDKoUMzKbBtGY5owYibjydukmOdilksm-M",
            "AIzaSyCL60GZMTlJZeKGLtaZQrqAJ_W2XpDRRmA",

            "AIzaSyB-Zi49hIZvnQ9gYSu17HOma5dsJJS5lzU",
            "AIzaSyCiIM6jXU85muW1vh9WVpyZ5QVUuc29a_8",
            "AIzaSyDgxHPZ10hiSN3Lu8MPyUP_JMn4cUsN5s0",
            "AIzaSyDIDmCHkbnLARfRxCQAUZqLa9z_PMAf9Ys",
            "AIzaSyA3LPPwIqplugfQDg2Z-YInpitJ2I6zbws",
            "AIzaSyAZkJoo7GUviUBnrozoxmR86hLc0jNaynI",
            "AIzaSyBfz9Rfu4VNDORqB7STHTte3PCI34zeq3I",
            "AIzaSyDy1DwL-gh6KOHdi032RFcMzFY4tTnj6r8",
            "AIzaSyB741ULpXm4pIF3m8jRS5UXeTY5RNyv9f4",
            "AIzaSyB0Qs6Pm7cwOXT-AQRD_-Tag0DteXpeKzg",
            "AIzaSyC0fJh6RJQvom2TmSzwr2poGqS0tEXuoho",
            "AIzaSyDz1v7BlghDDAE0VQcJCIYZXF34Ws-V8SY",
            "AIzaSyCYGRTJYEJc_5_JCgoZi0oo00JTbsElEKw",

            "AIzaSyAw9PjNRDq6_7B4Wb0b9L6Amp6BY3oU-qA",
            "AIzaSyD1xaJuIsW95V4C3GpMVRjISH-8gHm59s0",
            "AIzaSyCj09WkGycYeSwsZoptqGW-hAdzDVU9aW4",
            "AIzaSyAasTHpShXN8po3aadEqxNqTXFx-VzXm0A",
            "AIzaSyBGXrYQ8aqpoSQ4LklSlQ3pTFhbvlr-_aE",
            "AIzaSyDoLQbfjw5j1lUCfSCA8tXc7MgDIAzfXuA",
            "AIzaSyBho-4wN1-_gwCHsE7UBvXkV5aBKVSaR88",
            "AIzaSyD4SdsJ22_g4nHVXIryWVunMjouoEYZzLs",
            "AIzaSyBj7pCukzRxToaY-Hv8-MJSZQ14sqON2bo",
            "AIzaSyC2gQ8gD5wcGYXdatZzvtAeOdE9FqneTr0",

            "AIzaSyAGGhsolhl9cxn8U-hhNeSA9drdGkso-NI",
            "AIzaSyCIAVKDIwNsuVMGFuW-RUJbQH7Df4L1ZbM",
            "AIzaSyAryokbKFOwQMNTLVwc3-FSo17V2Fp5Nds",
            "AIzaSyBRRHbD1tabLv5fTZppYAk5W-rhCN1tc5w",
            "AIzaSyAH6o8a4GghcNb2lG7lHwV3518mO0d3OnE",
            "AIzaSyAvawsEOAz36S4mCqiGIMSaimXiwy_ULKo",
            "AIzaSyDSZ9RvglTXiYeRP1P2RoQzUi6NQfj-Xj8",
            "AIzaSyA2os7O3AcRn_8kjW9kxa76j3OS9hcDEFE",
            "AIzaSyAs2x-5IDVDfORzz3jEGAnTOfPrE5nKWeA",
            "AIzaSyCtOi-aRoelcKvDsn9HLk5sQB-blvrfmuY",

            "AIzaSyDtr4ISDU9R2EZVAtQd1URumrGn88kfT_A",
            "AIzaSyAeCg9PkR45hPRV0jYwudLrCZ4aKeNLJjk",
            "AIzaSyC5YGMQAKpONxzmM5j4l-nBdj4zYVhGc-g",
            "AIzaSyBGKOvcAreW-Y1XRLHYFQjrqr6Garty7M8",
            "AIzaSyAOyCO1XDVo_ReqES7agVujAFmZRssz3a0",
            "AIzaSyCaT94Ddne03nXwi854KplqN-qfy7eRoRk"
    };
    public static final int FINDING_PATH_COUNT = 100;


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
