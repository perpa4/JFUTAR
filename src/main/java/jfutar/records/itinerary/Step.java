package jfutar.records.itinerary;

import jfutar.records.stop.Stop;

/**
 * Ez az interface az alapja a WalkStep, illetve a RouteStep osztályoknak
 * (Mivel egy itinerary Routeokat tartalmaz lényegében).
 */
public sealed interface Step permits WalkStep, RouteStep {
    String getMode();
    Stop fromStop();
    long startTime();
    long endTime();
}