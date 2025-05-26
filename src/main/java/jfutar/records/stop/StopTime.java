package jfutar.records.stop;

/**
 * Ez az osztály felelős egy menetrend szerinti megállás eltárolásáért.
 *
 * @param stop a megálló, ahol történik az esemény
 * @param departureTime indulási idő
 */
public record StopTime(
        Stop stop,
        long departureTime
) { }
