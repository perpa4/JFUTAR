package jfutar.managers;

import javafx.animation.ScaleTransition;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import jfutar.records.stop.Stop;
import jfutar.records.trip.SaveableTrip;
import jfutar.views.planner.HistoryView;
import jfutar.views.planner.SearchView;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

/**
 * Ez az osztály felelős a random metódusok tárolásáért.
 * <p>
 * Ide tartoznak UI-elemek készítése és egyszerű segédfüggvények, mint az időformázás.
 */
public final class UtilityManager {
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    /**
     * Egy Unix timestamp (másodperc) átalakítása HH:mm formátumú időre.
     *
     * @param unixTimeStamp másodpercben mért időbélyeg
     * @return az idő formázott szövege
     */
    public static String formatTime(long unixTimeStamp) {
        Instant instant = Instant.ofEpochSecond(unixTimeStamp);
        return formatter.format(instant);
    }

    /**
     * Létrehoz egy vízszintes elválasztó vonalat (1px magas).
     *
     * @return a generált {@link Region} elem
     */
    public static Region createSeparator() {
        Region line = new Region();
        line.setPrefHeight(1);
        line.setMaxWidth(Double.MAX_VALUE);
        line.setStyle("-fx-background-color: #ddd;");
        return line;
    }

    /**
     * Létrehoz egy vissza gombot, amely visszalép az előző view-re.
     *
     * @return a létrehozott {@link Button}
     */
    public static Button createBackButton() {
        Button button = new Button();
        button.getStyleClass().addAll("circle-button", "clickable");
        SVGPath icon = new SVGPath();
        icon.setContent("M7 12L17 12M7 12L11 8M7 12L11 16");
        icon.setStroke(Color.WHITE);
        icon.setStrokeWidth(3);
        button.setGraphic(icon);
        button.setOnAction(_ -> SceneManager.back());
        addHoverScaleEffect(button, 1.15);
        return button;
    }

    /**
     * Létrehoz egy előzmények gombot, amely megnyitja a {@link HistoryView} nézetet.
     *
     * @return a létrehozott kereső gomb
     */
    public static Button createHistoryButton(String date, String time, String modes, Boolean arriveBy, Boolean wheelchair) {
        Button button = new Button();
        button.getStyleClass().addAll("circle-button-white", "clickable");
        SVGPath icon = new SVGPath();
        icon.setContent("M5.60423 5.60423L5.0739 5.0739V5.0739L5.60423 5.60423ZM4.33785 6.87061L3.58786 6.87438C3.58992 7.28564 3.92281 7.61853 4.33408 7.6206L4.33785 6.87061ZM6.87963 7.63339C7.29384 7.63547 7.63131 7.30138 7.63339 6.88717C7.63547 6.47296 7.30138 6.13549 6.88717 6.13341L6.87963 7.63339ZM5.07505 4.32129C5.07296 3.90708 4.7355 3.57298 4.32129 3.57506C3.90708 3.57715 3.57298 3.91462 3.57507 4.32882L5.07505 4.32129ZM3.75 12C3.75 11.5858 3.41421 11.25 3 11.25C2.58579 11.25 2.25 11.5858 2.25 12H3.75ZM16.8755 20.4452C17.2341 20.2378 17.3566 19.779 17.1492 19.4204C16.9418 19.0619 16.483 18.9393 16.1245 19.1468L16.8755 20.4452ZM19.1468 16.1245C18.9393 16.483 19.0619 16.9418 19.4204 17.1492C19.779 17.3566 20.2378 17.2341 20.4452 16.8755L19.1468 16.1245ZM5.14033 5.07126C4.84598 5.36269 4.84361 5.83756 5.13505 6.13191C5.42648 6.42626 5.90134 6.42862 6.19569 6.13719L5.14033 5.07126ZM18.8623 5.13786C15.0421 1.31766 8.86882 1.27898 5.0739 5.0739L6.13456 6.13456C9.33366 2.93545 14.5572 2.95404 17.8017 6.19852L18.8623 5.13786ZM5.0739 5.0739L3.80752 6.34028L4.86818 7.40094L6.13456 6.13456L5.0739 5.0739ZM4.33408 7.6206L6.87963 7.63339L6.88717 6.13341L4.34162 6.12062L4.33408 7.6206ZM5.08784 6.86684L5.07505 4.32129L3.57507 4.32882L3.58786 6.87438L5.08784 6.86684ZM12 3.75C16.5563 3.75 20.25 7.44365 20.25 12H21.75C21.75 6.61522 17.3848 2.25 12 2.25V3.75ZM12 20.25C7.44365 20.25 3.75 16.5563 3.75 12H2.25C2.25 17.3848 6.61522 21.75 12 21.75V20.25ZM16.1245 19.1468C14.9118 19.8483 13.5039 20.25 12 20.25V21.75C13.7747 21.75 15.4407 21.2752 16.8755 20.4452L16.1245 19.1468ZM20.25 12C20.25 13.5039 19.8483 14.9118 19.1468 16.1245L20.4452 16.8755C21.2752 15.4407 21.75 13.7747 21.75 12H20.25ZM6.19569 6.13719C7.68707 4.66059 9.73646 3.75 12 3.75V2.25C9.32542 2.25 6.90113 3.32791 5.14033 5.07126L6.19569 6.13719Z");
        icon.setStroke(Color.web("#C3C3C3"));
        icon.setFill(null);
        icon.setStrokeWidth(1.8);
        button.setGraphic(icon);
        button.setOnAction(_ -> SceneManager.show(new HistoryView(date, time, modes, arriveBy, wheelchair)));
        addHoverScaleEffect(button, 1.15);
        return button;
    }

    /**
     * Létrehoz egy kereső gombot, amely megnyitja a {@link SearchView} nézetet.
     *
     * @param date az aktuális dátum, amelyet a keresőbe továbbít
     * @return a létrehozott kereső gomb
     */
    public static Button createSearchButton(String date) {
        Button button = new Button();
        button.getStyleClass().addAll("circle-button-white", "clickable");
        SVGPath icon = new SVGPath();
        icon.setContent("M15.7955 15.8111L21 21M18 10.5C18 14.6421 14.6421 18 10.5 18C6.35786 18 3 14.6421 3 10.5C3 6.35786 6.35786 3 10.5 3C14.6421 3 18 6.35786 18 10.5Z");
        icon.setStroke(Color.web("#C3C3C3"));
        icon.setFill(null);
        icon.setStrokeWidth(3);
        button.setGraphic(icon);
        button.setOnAction(_ -> SceneManager.show(new SearchView(date)));
        addHoverScaleEffect(button, 1.15);
        return button;
    }

    /**
     * Hozzáadja vagy eltávolítja a megadott megállót a kedvencek közül.
     * A gomb stílusa automatikusan frissül.
     *
     * @param stop a megálló objektum
     * @param button a hozzátartozó UI gomb
     */
    public static void toggleFavoriteStop(Stop stop, Button button) {
        if (FavoritesManager.addFavoriteStop(stop)) {
            System.out.println("Hozzáadva a kedvencekhez!");
            button.getStyleClass().add("favorites-button-active");
            System.out.println("Hozzáadott megálló: " + stop.getName() + " (" + stop.getId() + ")");
        } else {
            if (FavoritesManager.removeFavoriteStop(stop)) {
                System.out.println("Törölve a kedvencekből!");
                button.getStyleClass().remove("favorites-button-active");
                System.out.println("Törölt megálló: " + stop.getName() + " (" + stop.getId() + ")");
            } else {
                System.out.println("Hát ezt a hibát meg hogy kaptad... XD");
            }
        }
    }

    /**
     * Hozzáadja vagy eltávolítja a megadott útvonalat a kedvencek közül.
     * A gomb stílusa automatikusan frissül.
     *
     * @param trip a kedvenc útvonal
     * @param button a hozzátartozó UI gomb
     */
    public static void toggleFavoriteTrip(SaveableTrip trip, Button button) {
        if (FavoritesManager.addFavoriteTrip(trip)) {
            System.out.println("Hozzáadva a kedvencekhez!");
            button.getStyleClass().add("favorites-button-active");
            System.out.println("Hozzáadott útvonal: " + trip.getFrom().getName() + " - " + trip.getTo().getName());
        } else {
            if (FavoritesManager.removeFavoriteTrip(trip)) {
                System.out.println("Törölve a kedvencekből!");
                button.getStyleClass().remove("favorites-button-active");
                System.out.println("Törölt útvonal: " + trip.getFrom().getName() + " - " + trip.getTo().getName());
            } else {
                System.out.println("Hát ezt a hibát meg hogy kaptad... XD");
            }
        }
    }

    /**
     * Hover-animációt ad egy gombhoz: egér fölé vitelkor enyhén megnő.
     *
     * @param parent a cél gomb
     * @param strenght az effekt erőssége
     */
    public static void addHoverScaleEffect(Parent parent, Double strenght) {
        parent.setOnMouseEntered(_ -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), parent);
            st.setToX(1 * strenght);
            st.setToY(1 * strenght);
            st.play();
        });

        parent.setOnMouseExited(_ -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), parent);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    // Ez nem készült el végül, azóta meg elfelejtettem mit akartam vele xd
    /*public static <T> void runAsyncTask(
            Supplier<T> supplier, // Átadjuk
            Consumer<T> onSuccess, // Átvesszük
            Consumer<Throwable> onFailure // Átvesszük
    ) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return supplier.get();
            }
        };

        task.setOnSucceeded(_ -> onSuccess.accept(task.getValue()));
        task.setOnFailed(_ -> onFailure.accept(task.getException()));

        new Thread(task).start();
    }*/

    /**
     * Egy {@code Predicate}, amely ellenőrzi, hogy egy megálló szerepel-e a kedvencek között.
     * Ez a feltétel {@code true}-t ad vissza, ha a megadott {@link Stop} objektum
     * szerepel a {@link FavoritesManager} kedvenc megállói között.
     * Használható például megállók listájának szűrésére vagy UI elemek megjelenítésének
     * feltételeként.
     * Azért létezik, mert szebb kódot fog eredményezni, és mert szeretném gyakorolni a Functional Interfacek használatát.
     *
     * @see FavoritesManager#getFavoriteStops()
     */
    public static final Predicate<Stop> isFavoriteStop =
            stop -> FavoritesManager.getFavoriteStops().contains(stop);

    /**
     * Egy {@code Predicate}, amely ellenőrzi, hogy egy útvonal szerepel-e a kedvencek között.
     * Ez a feltétel {@code true}-t ad vissza, ha a megadott {@link SaveableTrip} objektum
     * szerepel a {@link FavoritesManager} kedvenc útvonalai között.
     * Használható például útvonalak listájának szűrésére vagy UI elemek megjelenítésének
     * feltételeként.
     * Azért létezik, mert szebb kódot fog eredményezni, és mert szeretném gyakorolni a Functional Interfacek használatát.
     *
     * @see FavoritesManager#getFavoriteTrips()
     */
    public static final Predicate<SaveableTrip> isFavoriteTrip=
            favoriteTrip -> FavoritesManager.getFavoriteTrips().contains(favoriteTrip);
}
