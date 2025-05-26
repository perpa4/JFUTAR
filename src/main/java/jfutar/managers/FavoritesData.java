package jfutar.managers;

import jfutar.records.stop.Stop;
import jfutar.records.trip.SaveableTrip;

import java.util.List;

/**
 * Ez az osztály felelős a kedvencek tárolásáért.
 * <p>
 * Használja a {@link FavoritesManager}-t, és YAML-fájlba mentéshez/olvasáshoz
 * használatos adatszerkezetként szolgál.
 */
public final class FavoritesData {
    private List<Stop> stops;
    private List<SaveableTrip> trips;

    public FavoritesData() { }

    /**
     * Visszaadja a kedvenc megállók listáját.
     *
     * @return kedvenc megállók listája
     */
    public List<Stop> getStops() {
        return stops;
    }

    /**
     * Beállítja a kedvenc megállók listáját.
     *
     * @param stops új megálló lista
     */
    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }

    /**
     * Visszaadja a kedvenc utak listáját.
     *
     * @return kedvenc utak listája
     */
    public List<SaveableTrip> getTrips() {
        return trips;
    }

    /**
     * Beállítja a kedvenc utak listáját.
     *
     * @param trips új utak listája
     */
    public void setTrips(List<SaveableTrip> trips) {
        this.trips = trips;
    }
}
