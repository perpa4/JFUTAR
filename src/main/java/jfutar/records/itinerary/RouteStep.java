package jfutar.records.itinerary;

import jfutar.records.trip.Route;
import jfutar.records.stop.Stop;

/**
 * Ez az rekord implementálja a Stepet.
 * Eltárolja az olyan lépések paramétereit, amik valamilyen public transportot vesznek igénybe.
 */
public record RouteStep(
        Route route,
        String tripId,
        long startTime,
        long endTime,
        Stop fromStop
) implements Step {
    @Override
    public String getMode() {
        return route.getMode();
    }
}
