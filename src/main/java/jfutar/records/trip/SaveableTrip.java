package jfutar.records.trip;

import jfutar.records.stop.Stop;

/**
 * Ez az osztály felelős a kedvenc útvonalak adatainak tárolásáért.
 * SnakeYAML miatt nem final változókat tartalmaz, mert kell setter.
 * Két megállót (kiinduló és cél) tárol, amelyeket a felhasználó kedvencként jelölt meg.
 */
public class SaveableTrip {
    private Stop from;
    private Stop to;

    public SaveableTrip() {
        // SnakeYAML-nek kell üres konstruktor bruh
    }

    /**
     * Létrehoz egy új kedvenc útvonalat a megadott megállópárral.
     *
     * @param from a kiinduló megálló
     * @param to a cél megálló
     */
    public SaveableTrip(Stop from, Stop to) {
        this.from = from;
        this.to = to;
    }

    public Stop getFrom() {
        return from;
    }

    public void setFrom(Stop from) {
        this.from = from;
    }

    public Stop getTo() {
        return to;
    }

    public void setTo(Stop to) {
        this.to = to;
    }

    /**
     * Stringként visszaadja az útvonalat a megállók neve alapján.
     *
     * @return pl. "Blaha ➔ Keleti"
     */
    @Override
    public String toString() {
        return from.getName() + " ➔ " + to.getName();
    }

    /**
     * Két útvonalat akkor tekintünk egyenlőnek, ha ugyanaz a from és to megállójuk.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SaveableTrip that = (SaveableTrip) o;
        return from.equals(that.from) && to.equals(that.to);
    }

    @Override
    public int hashCode() {
        return from.hashCode() + to.hashCode();
    }
}
