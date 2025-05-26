package jfutar.records.stop;

/**
 * Ez az osztály felelős a megállók adatainak tárolásáért,
 * aminek a segítségével le tudjuk kérdezni az útvonal lehetőségeket.
 * SnakeYAML miatt nem final változókat tartalmaz, mert kell setter.
 */
public final class Stop {
    private String name;
    private String id;
    private String postcode;
    private String subTitle;
    private double lat;
    private double lon;

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public Stop(String name, String id, double lat, double lon) {
        this.name = name;
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }

    public Stop() {}

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getVertex() {
        return "BKK:" + id;
    }

    public String getPlaceParam() {
        return name + "::" + getVertex();
    }

    public String getPlaceParam2() {
        return name + "::" + lat + "," + lon;}

    @Override
    public String toString() {
        return name + " (ID: " + id + ")";
    }

    // Kell a containshez
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Stop stop = (Stop) obj;
        return id.equals(stop.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
