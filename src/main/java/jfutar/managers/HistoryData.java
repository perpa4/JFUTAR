package jfutar.managers;

import jfutar.records.stop.Stop;
import jfutar.records.trip.SaveableTrip;

import java.util.List;

/**
 * Ez az osztály felelős a megtekintési előzmények tárolásáért.
 * <p>
 * Használja a {@link HistoryManager}-t, és YAML-fájlba történő mentéshez/olvasáshoz
 * szolgál adatszerkezetként.
 */
public final class HistoryData {
    private List<Stop> stops;
    private List<SaveableTrip> trips;

    public HistoryData() { }

    /**
     * Visszaadja a legutóbb megtekintett megállók listáját.
     *
     * @return megállók listája
     */
    public List<Stop> getStops() {
        return stops;
    }

    /**
     * Beállítja a megtekintett megállók listáját.
     *
     * @param stops új megálló lista
     */
    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }

    /**
     * Visszaadja a legutóbb megtekintett utak listáját.
     *
     * @return utak listája
     */
    public List<SaveableTrip> getTrips() {
        return trips;
    }

    /**
     * Beállítja a megtekintett utak listáját.
     *
     * @param trips új utak listája
     */
    public void setTrips(List<SaveableTrip> trips) {
        this.trips = trips;
    }
}
