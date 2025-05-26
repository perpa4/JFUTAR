package jfutar.records.stop;

import jfutar.records.trip.Route;

/**
 * Ez a rekord felelős egy érkezés vagy indulás adatainak tárolásáért.
 *
 * @param route a járat, amelyhez tartozik
 * @param tripId az utazás azonosítója
 * @param stopId a megálló azonosítója
 * @param departureTime indulás időpontja
 */
public record Departure(
        Route route,
        String tripId,
        String stopId,
        long departureTime
) { }