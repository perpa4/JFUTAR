package jfutar.records.itinerary;

import jfutar.records.stop.Stop;

/**
 * Ez az rekord implementálja a Stepet.
 * Eltárolja a séta paramétereit, pl. távolság, idő, indulás, érkezés.
 */
public record WalkStep(double distance, int walkTime, long startTime, long endTime, Stop fromStop) implements Step {

    @Override
    public String getMode() {
        return "WALK";
    }
}
