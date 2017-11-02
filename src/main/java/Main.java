import adapters.GMapsPlannerAdapter;
import model.Address;
import model.Leg;
import model.Route;
import model.TransportMode;
import java.sql.Timestamp;

public class Main {
    public static void main(String[] args) {

        GMapsPlannerAdapter adapter = new GMapsPlannerAdapter();

        // + 1 hour
        int delay = 3600000;
        Timestamp arrival = new Timestamp(System.currentTimeMillis() + delay);

        Address prague = new Address(50.099260,14.383054);
        Address boleslav = new Address(50.409845,14.915914);
        TransportMode mode = TransportMode.UNKNOWN;

        Route bestRoute = adapter.findBestRoute(boleslav,prague,mode,arrival);
        System.out.println(bestRoute);
    }
}
