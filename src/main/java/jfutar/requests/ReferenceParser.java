package jfutar.requests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jfutar.records.trip.Route;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ReferenceParser {
    private ReferenceParser() { }
    /**
     * Kinyeri a Route objektumokat a JSON "references.routes" szekcióból.
     *
     * @param data a teljes API válasz "data" mezője
     * @return Map routeId -> Route
     */
    public static Map<String, Route> parseRoutes(JsonObject data) {
        if (data == null || !data.has("references")) return Map.of();
        JsonObject references = data.getAsJsonObject("references");
        if (!references.has("routes")) return Map.of(); // Üres map ami nem változtatható
        JsonObject routeEntries = references.getAsJsonObject("routes");

        // <String,Route> Function<T,R>
        // T: Map.Entry<String, JsonElement>
        // R: Route
        // Ugye itt a key az String, a value az egy JsonElement lesz, amiből felépítjük a route-ot
        Function<Map.Entry<String, JsonElement>, Route> entryToRoute = entry -> {
            JsonObject routeObj = entry.getValue().getAsJsonObject();
            String routeId = entry.getKey();

            String shortName = routeObj.has("shortName") ? routeObj.get("shortName").getAsString() : "?";
            String headsign = routeObj.has("description") ? routeObj.get("description").getAsString() : "?";
            String mode = routeObj.has("type") ? routeObj.get("type").getAsString() : "?";
            String color = routeObj.has("color") ? "#" + routeObj.get("color").getAsString() : "#000000";
            String textColor = routeObj.has("textColor") ? "#" + routeObj.get("textColor").getAsString() : "#FFFFFF";

            return new Route(mode, routeId, shortName, headsign, color, textColor);
        };

        // <String,Route> stream amiből felépítjük a mapet
        return routeEntries.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entryToRoute
                ));
    }
    /**
     * Kinyeri a Stop neveket a JSON "references.stops" szekcióból.
     *
     * @param data a teljes API válasz "data" mezője
     * @return Map stopId -> stopName
     */
    public static Map<String, String> parseStops(JsonObject data) {
        if (data == null || !data.has("references")) return Map.of();
        JsonObject references = data.getAsJsonObject("references");
        if (!references.has("stops")) return Map.of(); // Üres map ami nem változtatható
        JsonObject stopEntries = references.getAsJsonObject("stops");

        // <String,Stop> Function<T,R>
        // T: Map.Entry<String, JsonElement>
        // R: String
        // Átalakítja T-t R-é
        // Kiolvassa a neveket
        Function<Map.Entry<String, JsonElement>, String> entryToStopName = entry -> {
            JsonObject stopObj = entry.getValue().getAsJsonObject();
            // Kiolvassa a nevet az objektumbó aztán returnöli. HA VAN.
            return stopObj.has("name") ? stopObj.get("name").getAsString() : "?";
        };

        // Létrehoz egy Map<String,String>-et, a
        return stopEntries.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, // kulcsa marad
                        entryToStopName // Itt van felhasználva a NÉV
                ));
    }
}
