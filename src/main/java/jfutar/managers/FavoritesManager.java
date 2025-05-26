package jfutar.managers;

import jfutar.records.stop.Stop;

import jfutar.records.trip.SaveableTrip;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.inspector.TagInspector;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ez az osztály felelős a kedvencek kezeléséért.
 * YAML fájlba menti a kedvenceket, mert szerintem sokkal átláthatóbb, és könnyebb is használni mint a gson-t.
 */
public final class FavoritesManager {
    // Fájl neve és a tároló listák
    private static final String FAVORITES_FILE = "favorites.yaml";
    private static List<Stop> favoriteStops = new ArrayList<>();
    private static List<SaveableTrip> favoriteTrips = new ArrayList<>();

    // YAML objektum
    private static final Yaml yaml;
    // Static block mert szebb minthogy függvényt írok rá
    // https://stackoverflow.com/questions/76717530/global-tag-is-not-allowed-exception-in-snakeyaml-2-0
    static {
        LoaderOptions loaderOptions = new LoaderOptions();
        TagInspector tagInspector = tag -> tag.getClassName().equals(FavoritesData.class.getName());
        loaderOptions.setTagInspector(tagInspector);

        yaml = new Yaml(new Constructor(FavoritesData.class, loaderOptions));
    }

    /**
     * A kedvencek betöltése a YAML fájlból a memóriába.
     * Ha a fájl nem létezik vagy hiba történik, üres listák maradnak.
     */
    public static void loadFavorites() {
        try (FileReader reader = new FileReader(FAVORITES_FILE)) {
            FavoritesData favoritesData = yaml.loadAs(reader, FavoritesData.class);
            if (favoritesData != null) {
                favoriteStops = favoritesData.getStops() != null ? favoritesData.getStops() : new ArrayList<>();
                favoriteTrips = favoritesData.getTrips() != null ? favoritesData.getTrips() : new ArrayList<>();
            }
            System.out.println(favoriteStops);
            System.out.println(favoriteTrips);
        }
        catch (Exception e) {
            System.out.println("Nincsenek kedvencek!");
        }
    }

    /**
     * A jelenlegi kedvencek (megállók és utak) mentése a YAML fájlba.
     */
    public static void saveFavorites() {
        try (FileWriter writer = new FileWriter(FAVORITES_FILE)) {
            FavoritesData data = new FavoritesData();
            data.setStops(favoriteStops);
            data.setTrips(favoriteTrips);
            yaml.dump(data, writer);
        }
        catch (Exception e) {
            System.out.println("Hiba történt: " + e.getMessage());
        }
    }

    /**
     * Hozzáad egy megállót a kedvencekhez.
     *
     * @param stop a hozzáadandó megálló
     * @return {@code true} ha sikerült hozzáadni, {@code false} ha már benne volt
     */
    public static boolean addFavoriteStop(Stop stop) {
        if (!favoriteStops.contains(stop)) {
            favoriteStops.add(stop);
            saveFavorites();
            return true; // sikeresen hozzáadva
        }
        return false; // már létezett
    }

    /**
     * Eltávolít egy megállót a kedvencek közül.
     *
     * @param stop a törlendő megálló
     * @return {@code true} ha sikerült törölni, {@code false} ha nem volt benne
     */
    public static boolean removeFavoriteStop(Stop stop) {
        if (favoriteStops.contains(stop)) {
            favoriteStops.remove(stop);
            saveFavorites();
            return true; // sikeresen törölve
        }
        return false; // ha nincs benne
    }

    /**
     * Visszaadja az összes kedvenc megállót.
     *
     * @return a kedvenc megállók listája
     */
    public static List<Stop> getFavoriteStops() {
        return favoriteStops;
    }

    /**
     * Hozzáad egy útvonalat a kedvencekhez.
     *
     * @param trip a kedvenc útvonal
     * @return {@code true} ha újként lett hozzáadva, {@code false} ha már benne volt
     */
    public static boolean addFavoriteTrip(SaveableTrip trip) {
        if (!favoriteTrips.contains(trip)) {
            favoriteTrips.add(trip);
            saveFavorites();
            return true;
        }
        return false;
    }

    /**
     * Eltávolít egy útvonalat a kedvencek közül.
     *
     * @param trip a törlendő útvonal
     * @return {@code true} ha sikerült eltávolítani, {@code false} ha nem volt benne
     */
    public static boolean removeFavoriteTrip(SaveableTrip trip) {
        if (favoriteTrips.contains(trip)) {
            favoriteTrips.remove(trip);
            saveFavorites();
            return true;
        }
        return false;
    }

    /**
     * Visszaadja az összes kedvenc útvonalat.
     *
     * @return a kedvenc utak listája
     */
    public static List<SaveableTrip> getFavoriteTrips() {
        return favoriteTrips;
    }
}
