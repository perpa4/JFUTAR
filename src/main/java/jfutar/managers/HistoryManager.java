package jfutar.managers;

import jfutar.records.stop.Stop;
import jfutar.records.trip.SaveableTrip;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.inspector.TagInspector;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Ez az osztály felelős az előzmények kezeléséért.
 * YAML fájlba menti az előzményeket, mert szerintem sokkal átláthatóbb, és könnyebb is használni mint a gson-t.
 */
public final class HistoryManager {
    // Fájl neve és a tároló listák
    private static final String HISTORY_FILE  = "history.yaml";
    private static final int MAX_HISTORY_SIZE = 5;

    private static List<Stop> recentStops = new ArrayList<>();
    private static List<SaveableTrip> recentTrips = new ArrayList<>();

    // YAML objektum
    private static final Yaml yaml;
    // Static block mert szebb minthogy függvényt írok rá
    // https://stackoverflow.com/questions/76717530/global-tag-is-not-allowed-exception-in-snakeyaml-2-0
    static {
        LoaderOptions loaderOptions = new LoaderOptions();
        TagInspector tagInspector = tag -> tag.getClassName().equals(HistoryData.class.getName());
        loaderOptions.setTagInspector(tagInspector);

        yaml = new Yaml(new Constructor(HistoryData.class, loaderOptions));
    }

    /**
     * Az előzmények betöltése a YAML fájlból a memóriába.
     * Ha a fájl nem létezik vagy hiba történik, üres listák maradnak.
     */
    public static void loadHistory() {
        try (FileReader reader = new FileReader(HISTORY_FILE)) {
            HistoryData data = yaml.loadAs(reader, HistoryData.class);
            if (data != null) {
                recentStops = data.getStops() != null ? data.getStops() : new ArrayList<>();
                recentTrips = data.getTrips() != null ? data.getTrips() : new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("Nincs korábbi előzmény!");
        }
    }

    /**
     * A jelenlegi előzmények (megállók és utak) mentése a YAML fájlba.
     */
    public static void saveHistory() {
        try (FileWriter writer = new FileWriter(HISTORY_FILE)) {
            HistoryData data = new HistoryData();
            data.setStops(recentStops);
            data.setTrips(recentTrips);
            yaml.dump(data, writer);
        } catch (Exception e) {
            System.out.println("Nem sikerült elmenteni az előzményeket: " + e.getMessage());
        }
    }

    /**
     * Hozzáad egy megállót az előzményekhez.
     *
     * @param stop a hozzáadandó megálló
     */
    public static void addStopToHistory(Stop stop) {
        recentStops.remove(stop); // hogy ne legyen duplikáció
        recentStops.addFirst(stop); // mindig előre tesszük

        if (recentStops.size() > MAX_HISTORY_SIZE)
            recentStops = recentStops.subList(0, MAX_HISTORY_SIZE);

        saveHistory();
    }

    /**
     * Hozzáad egy útvonalat az előzményekhez.
     *
     * @param trip a hozzáadandó útvonalat
     */
    public static void addTripToHistory(SaveableTrip trip) {
        recentTrips.remove(trip);
        recentTrips.addFirst(trip);
        if (recentTrips.size() > MAX_HISTORY_SIZE)
            recentTrips = recentTrips.subList(0, MAX_HISTORY_SIZE);

        saveHistory();
    }

    /**
     * Visszaadja az összes előzmény megállót.
     *
     * @return az előzmény megállók listája
     */
    public static List<Stop> getRecentStops() {
        return recentStops;
    }

    /**
     * Visszaadja az összes előzmény útvonalat.
     *
     * @return az előzmény utak listája
     */
    public static List<SaveableTrip> getRecentTrips() {
        return recentTrips;
    }
}
