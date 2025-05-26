package jfutar.requests;

import com.google.gson.*;
import jfutar.records.search.SearchResult;
import jfutar.records.stop.Stop;
import jfutar.records.trip.Route;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static jfutar.requests.HttpUtils.*;
import static jfutar.requests.ReferenceParser.parseRoutes;
import static jfutar.views.Main.apiKey;

/**
 * Ez az osztály a keresési szöveg alapján címeket, megállókat vagy járatokat keres.
 * Az API válaszát feldolgozva visszaadja a találatokat {@link SearchResult} formában.
 */
public final class GetGeocode {

    /**
     * Cím vagy megálló keresése az adott szöveg alapján.
     *
     * @param query keresendő szöveg
     * @param all ha igaz, akkor járatokat is keres a megállókon kívül
     * @return a találatok (megállók és opcionálisan járatok) {@link SearchResult}-ként
     * @throws Exception ha az API hívás közben hiba történik
     */
    public static SearchResult getGeocode(String query, boolean all) throws Exception {
        List<Stop> stopResults = new ArrayList<>();
        List<Route> routeResults = new ArrayList<>();
        // Az APIban használatos URL
        String url = "https://futar.bkk.hu/api/geocoder/v1/geocode" +
                "?q=" + encode(query) +
                "&lang=hu" +

                "&types=stop-areas" +
                "&types=stops" +
                "&types=places" +
                (all ? "&types=routes" : "") +
                "&types=places&types=stop-areas" +
                "&key=" + apiKey +
                "&version=4" +
                "&appVersion=3.18.0";

        HttpResponse<String> response = sendRequest(url);
        JsonObject json = parseJson(response.body());
        JsonObject data = json.getAsJsonObject("data");
        if (data == null || !data.has("entry")) return new SearchResult(stopResults, new ArrayList<>());

        JsonObject entry = data.getAsJsonObject("entry");
        JsonObject places = entry.getAsJsonObject("places");

        // OTP helyek feldolgozása
        if (places.has("otp")) {
            JsonArray otpArray = places.getAsJsonArray("otp");
            for (int i = 0; i < Math.min(20, otpArray.size()); i++) {
                JsonObject place = otpArray.get(i).getAsJsonObject();
                String name = place.get("name").getAsString();
                String rawId = place.has("id") ? place.get("id").getAsString() : "N/A";
                String id = rawId.replace("stop_", "");  // BKK stop id prefix eltávolítás de hogy minek van???
                String postcode = place.has("subTitle") ? place.get("subTitle").getAsString().replaceAll("\\D", "") : "";

                double lat = place.get("lat").getAsDouble();
                double lon = place.get("lon").getAsDouble();

                Stop stop = new Stop(name, id, lat ,lon);
                stop.setPostcode(postcode);
                stopResults.add(stop);
            }
        }

        // OSM helyek feldolgozása
        if (places.has("osm")) {
            JsonArray osmArray = places.getAsJsonArray("osm");
            for (int i = 0; i < Math.min(20, osmArray.size()); i++) {
                JsonObject place = osmArray.get(i).getAsJsonObject();
                String name = place.has("name") ? place.get("name").getAsString() : "N/A";
                String id = place.has("id") ? place.get("id").getAsString() : "N/A"; // OSM ID
                String subTitle = place.has("subTitle") ? place.get("subTitle").getAsString() : "";

                double lat = place.get("lat").getAsDouble();
                double lon = place.get("lon").getAsDouble();

                Stop stop = new Stop(name, id, lat ,lon);
                stop.setSubTitle(subTitle);
                stopResults.add(stop);
            }
        }

        // Ha ALL, akkor fel kell dolgozni a járatokat is
        if (all) {
            // Beolvassuk azt a szar referenciák részt a válaszból mert nyílván külön kell tárolni, és nem a routeId mellett az adatokat, sokkal egyszerűbb és strukturáltabb lenne szerintem, bár ez nézőpont kérdése
            // Ez a Map csak párosításra lesz használva
            Map<String, Route> routeById = parseRoutes(data);

            if (entry.has("routeIds")) {
                JsonArray routeArray = entry.getAsJsonArray("routeIds");
                for (int i = 0; i < Math.min(20, routeArray.size()); i++) {
                    JsonObject routeRef = routeArray.get(i).getAsJsonObject();

                    String routeId = routeRef.has("routeId") ? routeRef.get("routeId").getAsString() : "?";

                    // Még nem tudunk részleteket, de később lekérheted őket külön API-val
                    Route routeObj = new Route(
                            routeById.get(routeId).getMode(),           // mode
                            routeId,                                    // routeId
                            routeById.get(routeId).getRouteShortName(), // short name feltételezve, amíg nincs más
                            routeById.get(routeId).getHeadsign(),       // headsign
                            routeById.get(routeId).getColor(),          // color
                            routeById.get(routeId).getTextColor()       // textColor
                    );
                    routeResults.add(routeObj);
                }
            }

        }

        // Debug
        // System.out.println("API válasz:\n" + response.body());

        return new SearchResult(stopResults, routeResults);
    }
}
