package jfutar.requests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jfutar.records.stop.Departure;
import jfutar.records.stop.StopInfo;
import jfutar.records.trip.Route;

import java.net.http.HttpResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static jfutar.requests.ReferenceParser.parseRoutes;
import static jfutar.views.Main.apiKey;
import static jfutar.requests.HttpUtils.*;

/**
 * Ez az osztály az adott megállóra lekérdezi az indulásokat és a járatokat.
 * Az FŐ célja, hogy visszaadja a {@link StopInfo} objektumot, amely tartalmazza
 * a következő indulásokat és a megállóhoz tartozó járatokat.
 * Ezen az osztályon végigmentem, és minden foreach alapú feldolgozást átalakítottam Function és Stream kombinációjává.
 */
public final class GetArrivalsAndDeparturesForStop {

    /**
     * Lekérdezi az indulásokat és járatokat egy adott megállóra.
     *
     * @param stopId a megálló azonosítója (pl. "BKK_CSF00065")
     * @return a megállóhoz tartozó indulások és járatok {@link StopInfo}-ban
     * @throws Exception ha hiba történik az API hívás közben
     */
    public static StopInfo getDeparturesForStop(String stopId) throws Exception {
        String url = "https://futar.bkk.hu/api/query/v1/ws/otp/api/where/arrivals-and-departures-for-stop.json" +
                "?includeReferences=agencies,routes,trips,stops,stations" +
                "&stopId=" + stopId +
                "&minutesBefore=1" +
                "&minutesAfter=30" +
                "&key=" + apiKey +
                "&version=4" +
                "&appVersion=3.18.0";

        HttpResponse<String> response = sendRequest(url);
        JsonObject json = parseJson(response.body());
        JsonObject data = json.getAsJsonObject("data");

        // Ha nincs entry
        if (data == null || !data.has("entry")) {
            System.out.println("Nincs 'entry' mező a válaszban.");
            return new StopInfo(List.of(), List.of()); // List.of az nem módosítható üres lista, minek is kellene módosítani ha üres
        }

        // Ha van entry
        JsonObject entry = data.getAsJsonObject("entry");
        if (!entry.has("routeIds") || !entry.has("stopTimes"))
            return new StopInfo(List.of(), List.of());

        // Tartalom
        JsonArray routeIds = entry.getAsJsonArray("routeIds");
        JsonArray stopTimes = entry.getAsJsonArray("stopTimes");
        JsonObject references = data.getAsJsonObject("references");
        JsonObject trips = references.getAsJsonObject("trips");

        // Beolvassuk azt a szar referenciák részt a válaszból mert nyílván külön kell tárolni, és nem a routeId mellett az adatokat, sokkal egyszerűbb és strukturáltabb lenne szerintem, bár ez nézőpont kérdése
        // Ez a Map csak párosításra lesz használva
        Map<String, Route> routeById = parseRoutes(data);

        // Departure Function<T,R>
        // T: JsonObject
        // R: Departure
        Function<JsonObject, Departure> jsonToDeparture = obj -> {
            String tripId = obj.get("tripId").getAsString();
            // Indulási idő eldöntése
            long time = obj.has("predictedDepartureTime")
                    ? obj.get("predictedDepartureTime").getAsLong()
                    : obj.get("departureTime").getAsLong();
            // RouteId-t kiszedjük a tripsből
            String routeId = trips.getAsJsonObject(tripId).get("routeId").getAsString();
            // Kiszedjük a routeot a mapből a key alapján
            Route r = routeById.get(routeId);
            // Beállítjuk setterrel a headsign-t mert nem kell ez konstruktorba más felhasználás miatt
            r.setHeadsign(trips.getAsJsonObject(tripId).get("tripHeadsign").getAsString());
            return new Departure(r, tripId, stopId, time);
        };

        // Departure Stream
        List<Departure> stopDepartures = StreamSupport.stream(stopTimes.spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                // StopTime-nak van e tripId mezője ILLETVE csak akkor megy tovább ha a JSON objektumbanm van tripId
                .filter(obj -> obj.has("tripId") && trips.has(obj.get("tripId").getAsString()))
                .map(jsonToDeparture)
                .toList(); // Listába collectoljuk

        List<Route> stopRoutes = StreamSupport.stream(routeIds.spliterator(), false)
                // Function<T,R>: ID a JSONElementből
                // T: JSONElement
                // R: String RouteID
                .map(JsonElement::getAsString)
                // Routeot megkapjuk
                // T: String RouteID
                // R: Route route
                .map(routeById::get)
                // Predicate-el megnézzük nem null-e
                .filter(Objects::nonNull)
                // Collecteljük listába
                .toList();

        return new StopInfo(stopDepartures, stopRoutes);
    }
}
