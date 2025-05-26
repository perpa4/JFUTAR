package jfutar.requests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jfutar.records.stop.StopTime;
import jfutar.records.stop.Stop;
import jfutar.records.trip.Trip;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static jfutar.requests.ReferenceParser.parseStops;
import static jfutar.requests.HttpUtils.*;
import static jfutar.views.Main.apiKey;

/**
 * Ez az osztály egy konkrét menet részleteit kéri le az API-ból.
 * A válasz alapján egy {@link Trip} objektumot ad vissza.
 */
public final class GetTripDetails {
    /**
     * Lekérdezi egy adott trip részletes menetrendi adatait.
     *
     * @param tripId az utazás azonosítója
     * @param fromStop a kezdő megálló
     * @param toStop a végállomás
     * @param date a dátum
     * @return a részletes trip {@link Trip} példányként
     * @throws Exception ha hiba történik a kérés közben
     */
    public static Trip getTripDetails(String tripId, Stop fromStop, Stop toStop, String date) throws Exception {
        String url = "https://futar.bkk.hu/api/query/v1/ws/otp/api/where/trip-details" +
                "?tripId=" + tripId +
                "&date=" + date +
                "&key=" + apiKey +
                "&version=4" +
                "&appVersion=3.18.0";

        HttpResponse<String> response = sendRequest(url);
        JsonObject json = parseJson(response.body());
        JsonObject data = json.getAsJsonObject("data");
        JsonObject entry = data.getAsJsonObject("entry");
        JsonArray stopTimes = entry.getAsJsonArray("stopTimes");

        // Beolvassuk azt a szar referenciák részt a válaszból mert nyílván külön kell tárolni, és nem a megálló ID mellett a nevet
        // Ez a Map csak párosításra lesz használva
        Map<String, String> stopNamesById = parseStops(data);

        // StopTime lista
        List<StopTime> stopTimeList = new ArrayList<>();

        for (JsonElement elem : stopTimes) {
            JsonObject stopJson = elem.getAsJsonObject();

            long departureTime;
            // Ha van predicted akkor az lesz
            if (stopJson.has("predictedDepartureTime") && !stopJson.get("predictedDepartureTime").isJsonNull())
                departureTime = stopJson.get("predictedDepartureTime").getAsLong();
                // Ha van sima menetrendi akkor az lesz
            else if (stopJson.has("departureTime") && !stopJson.get("departureTime").isJsonNull())
                departureTime = stopJson.get("departureTime").getAsLong();
            else
                continue;

            String stopId = stopJson.get("stopId").getAsString();
            String stopName = stopNamesById.getOrDefault(stopId, "?");

            // Új megálló
            // Itt nem kellenek a koordináták, mivel már úgy is adott a megálló
            Stop stop = new Stop(stopName, stopId, 0, 0);
            // Új stoptime
            stopTimeList.add(new StopTime(stop, departureTime));
        }

        return new Trip(stopTimeList, fromStop, toStop, date);
    }
}
