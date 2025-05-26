package jfutar.records.trip;

/**
 * Ez az osztály egy járatot ír le (busz, metró, villamos, stb.).
 * Tartalmazza a járathoz tartozó azonosítókat, nevet, színt és útirányt.
 */
public class Route {
    private final String mode;
    private final String routeId;
    private final String routeShortName;
    private String headsign;

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    private final String color;
    private final String textColor;

    /**
     * Létrehoz egy új Route példányt.
     *
     * @param mode közlekedési mód (pl. BUS, METRO)
     * @param routeId a járat azonosítója
     * @param routeShortName a járat rövid neve (pl. 7)
     * @param headsign útirány
     * @param color a badge színe (pl. "#FF0000")
     * @param textColor a szöveg színe (pl. "#FFFFFF")
     */
    public Route(String mode, String routeId, String routeShortName, String headsign, String color, String textColor) {
        this.mode = mode;
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.headsign = headsign;
        this.color = color;
        this.textColor = textColor;
    }

    public String getMode() {
        return mode;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public String getHeadsign() {
        return headsign;
    }

    public String getColor() {
        return color;
    }

    public String getTextColor() {
        return textColor;
    }
}
