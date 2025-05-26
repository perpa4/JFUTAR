package jfutar.requests;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.google.gson.*;

/**
 * Egy alaposztály, amely a HTTP GET alapú API-hívások kezelését segíti.
 */
public final class HttpUtils {
    /**
     * URL-paraméter kódolása UTF-8 karakterkódolással.
     *
     * @param value a kódolandó szöveg (lehet {@code null})
     * @return az URL-safe formátumban kódolt szöveg, vagy üres string ha {@code null}
     */
    protected static String encode(String value) {
        return value != null ? URLEncoder.encode(value, StandardCharsets.UTF_8) : "";
    }

    /**
     * Előre konfigurált {@link HttpClient}, amelyet a leszármazott osztályok használhatnak lekérésekhez.
     * A kapcsolati időtúllépés alapértelmezetten 10 másodperc.
     */
    protected static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * HTTP GET kérés küldése a megadott URL-re.
     *
     * @param url a kérés cél URL-je
     * @return a válasz {@link HttpResponse} objektuma
     * @throws RuntimeException ha a kérés közben kivétel történik
     */
    protected static HttpResponse<String> sendRequest(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Hibakezelés és naplózás központilag
        if (response.statusCode() != 200) {
            System.err.println("Hiba az API válaszban");
            System.err.println("URL: " + url);
            System.err.println("Státusz: " + response.statusCode());
            System.err.println("Válasz: " + response.body());
            throw new IOException("Hiba történt a lekérdezés közben. Ez azért kell, hogy le bírjam autómatikusan állítani a requestet.");
        }

        return response;
    }

    /**
     * A megadott JSON formátumú szöveget {@link JsonObject} típussá alakítja.
     *
     * @param responseBody a JSON szöveg
     * @return a kinyert {@link JsonObject}
     * @throws JsonSyntaxException ha a szöveg nem érvényes JSON
     */
    protected static JsonObject parseJson(String responseBody) throws JsonSyntaxException {
        return JsonParser.parseString(responseBody).getAsJsonObject();
    }
}
