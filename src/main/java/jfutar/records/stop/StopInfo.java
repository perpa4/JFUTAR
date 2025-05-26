package jfutar.records.stop;

import jfutar.records.trip.Route;

import java.util.List;

/**
 * Ez a rekord tartalmazza egy megálló aktuális indulásait és az ott megálló járatokat.
 *
 * @param departures indulások listája
 * @param routes az ott közlekedő járatok
 */
public record StopInfo(
        List<Departure> departures,
        List<Route> routes
) { }
