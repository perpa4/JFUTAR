package jfutar.requests;

import com.google.gson.*;
import jfutar.records.itinerary.WalkStep;
import jfutar.records.itinerary.Itinerary;
import jfutar.records.stop.Stop;
import jfutar.records.itinerary.Step;
import jfutar.records.itinerary.RouteStep;
import jfutar.records.trip.Route;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static jfutar.views.Main.apiKey;
import static jfutar.requests.HttpUtils.*;

/**
 * Ez az osztály felelős az útvonaltervezésért.
 * Az API-ból útvonalterveket kér le két megálló között adott időpontra.
 */
public final class GetPlanTrip {
    /**
     * Útvonaltervek lekérése a megadott paraméterekkel.
     *
     * @param from indulási megálló
     * @param to cél megálló
     * @param date dátum
     * @param time idő
     * @param modesParam közlekedési módok vesszővel elválasztva (pl. "BUS,SUBWAY")
     * @param arriveBy ha igaz, akkor az érkezési időt veszi figyelembe
     * @param wheelchair ha igaz, akkor akadálymentes útvonalat keres
     * @return egy lista {@link Itinerary} objektumokkal
     * @throws Exception ha az API hívás nem sikerül
     */
    public static List<Itinerary> getItineraries(Stop from, Stop to, String date, String time, String modesParam, boolean arriveBy, boolean wheelchair) throws Exception {
        // Eredménylista
        List<Itinerary> result = new ArrayList<>();

        String fromPlace = !from.getId().contains("osm") ? encode(from.getPlaceParam()) : encode(from.getPlaceParam2());
        String toPlace = !to.getId().contains("osm") ? encode(to.getPlaceParam()) : encode(to.getPlaceParam2());

        // Az APIban használatos URL
        String url = "https://futar.bkk.hu/api/query/v1/ws/otp/api/where/plan-trip.json" +
                "?version=4" +
                "&appVersion=3.18.0" +
                "&includeReferences=true" +

                (from.getId().contains("osm") ? "&fromCoord:" + from.getLat() + "," + from.getLon() : "") +
                (to.getId().contains("osm") ? "&toCoord:" + to.getLat() + "," + to.getLon() : "") +

                "&date=" + encode(date) +
                "&time=" + encode(time) +
                "&arriveBy=" + arriveBy +
                "&wheelchair=" + wheelchair +
                "&fromPlace=" + fromPlace +
                "&toPlace=" + toPlace +
                "&mode=" + encode(modesParam) +
                "&maxTransfers=5" +
                "&showIntermediateStops=true" +
                "&key=" + apiKey;

        System.out.println(url);

        HttpResponse<String> response = sendRequest(url);
        JsonObject data = parseJson(response.body()).getAsJsonObject("data");

        // Ha null v nincs benne entry akk üres/nem módosítható listát adunk vissza
        if (data == null || !data.has("entry")) return List.of();
        JsonObject plan = data.getAsJsonObject("entry").getAsJsonObject("plan");
        // ha a plan null v nincs benne itineraries akk ugyanaz
        if (plan == null || !plan.has("itineraries")) return List.of();

        // Lekérdezzük a paramétereket
        JsonArray itineraries = plan.getAsJsonArray("itineraries");

        // Végigmegyünk az összesen
        // Milyen szép imperatív for ciklusos feldolgozás, nem akarom átalakítani functionallá
        for (JsonElement e : itineraries) {
            JsonObject itinerary = e.getAsJsonObject();

            long startTime = itinerary.get("startTime").getAsLong() / 1000;
            long endTime = itinerary.get("endTime").getAsLong() / 1000;
            int duration = itinerary.get("duration").getAsInt();

            // Az utazási lépések itt lesznek eltárolva egy listában aztán lekezelve
            List<Step> routeSummary = new ArrayList<>();
            // Minden egyes úthoz szummázva lesz a séta hossza
            double totalWalkDistance = 0.0;

            // Kiszedjük a legeket a jsonből
            JsonArray legs = itinerary.getAsJsonArray("legs");

            // Végigmegyünk rajtuk
            for (JsonElement legElem : legs) {
                JsonObject leg = legElem.getAsJsonObject();
                double distance = leg.has("distance") ? leg.get("distance").getAsDouble() : 0.0;
                long walkDurationMs = leg.has("duration") ? leg.get("duration").getAsLong() : 0;
                int walkTime = (int) Math.round(walkDurationMs / 60000.0);

                // Mód lekérése
                String mode = leg.get("mode").getAsString();

                String routeId = leg.has("routeId") ? leg.get("routeId").getAsString() : "?";
                String tripId = leg.has("tripId") ? leg.get("tripId").getAsString() : "?";
                String routeShortName = leg.has("routeShortName") ? leg.get("routeShortName").getAsString() : "?";
                String headsign = leg.has("headsign") ? leg.get("headsign").getAsString() : "?";
                long stepStartTime = leg.has("startTime") ? leg.get("startTime").getAsLong() : 0;
                long stepEndTime = leg.has("endTime") ? leg.get("endTime").getAsLong() : 0;

                // Színek, így nem kell enum meg semmi
                String routeColor = leg.has("routeColor") ? leg.get("routeColor").getAsString() : "000000";
                String routeTextColor = leg.has("routeTextColor") ? leg.get("routeTextColor").getAsString() : "FFFFFF";

                // Megálló
                JsonObject stop = leg.getAsJsonObject("from");
                String stopName = stop.has("name") ? stop.get("name").getAsString() : "?";
                String stopId = stop.has("stopId") ? stop.get("stopId").getAsString() : "?";
                // Itt nem kellenek a koordináták, mivel már úgy is adott a megálló
                Stop fromStop = new Stop(stopName, stopId, 0, 0);

                // Ha séta
                if (mode.equals("WALK")) {
                    totalWalkDistance += distance;
                    routeSummary.add(new WalkStep(distance, walkTime, stepStartTime, stepEndTime, fromStop));
                }
                // Egyéb esetben public transport
                else {
                    routeSummary.add(new RouteStep(
                            new Route(mode, routeId, routeShortName, headsign, "#" + routeColor, "#" + routeTextColor),
                            tripId, stepStartTime, stepEndTime, fromStop));
                }
            }

            result.add(new Itinerary(from, to, startTime, endTime, duration, totalWalkDistance, routeSummary));
        }

        return result;
    }
}