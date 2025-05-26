package jfutar.records.search;

import jfutar.records.stop.Stop;
import jfutar.records.trip.Route;

import java.util.List;

/**
 * Ez az osztály a keresési eredményeket tartalmazza.
 * A keresés visszaadhat megállókat és járatokat is, attól függ ez van-e engedélyezve.
 */
public final class SearchResult {
    private final List<Stop> stops;
    private final List<Route> routes;

    public SearchResult(List<Stop> stops, List<Route> routes) {
        this.stops = stops;
        this.routes = routes;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public List<Route> getRoutes() {
        return routes;
    }
}
