package jfutar.records.itinerary;

import jfutar.records.stop.Stop;
import java.util.List;

/**
 * Ez a rekord felelős egy útvonalterv eltárolásáért.
 * Tartalmazza a szakaszokat, pl. busz, villamos, metró vagy gyaloglás.
 *
 * @param from kiinduló megálló
 * @param to cél megálló
 * @param startTime indulási idő
 * @param endTime érkezési idő
 * @param duration időtartam
 * @param totalWalkDistance teljes séta hossza méterben
 * @param routeSummary a lépések listája (WalkStep vagy RouteStep)
 */
public record Itinerary(
        Stop from,
        Stop to,
        long startTime,
        long endTime,
        int duration,
        double totalWalkDistance,
        List<Step> routeSummary
) { }
