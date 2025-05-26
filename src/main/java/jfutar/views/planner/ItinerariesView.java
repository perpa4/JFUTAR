package jfutar.views.planner;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import jfutar.managers.HistoryManager;
import jfutar.managers.SceneManager;
import jfutar.records.itinerary.Itinerary;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import jfutar.records.stop.Stop;
import jfutar.records.itinerary.Step;
import jfutar.records.itinerary.RouteStep;
import jfutar.records.itinerary.WalkStep;
import jfutar.records.trip.SaveableTrip;
import jfutar.requests.GetPlanTrip;
import jfutar.views.AbstractView;
import jfutar.views.details.StopView;
import jfutar.views.details.TripView;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static jfutar.managers.UtilityManager.*;

/**
 * Ez a nézet felelős a két megálló közötti útvonaltervek megjelenítéséért.
 * Automatikusan frissül, és lehetőség van a kedvencként jelölésre vagy mentésre.
 */
public final class ItinerariesView extends AbstractView {
    private final Stop fromStop;
    private final Stop toStop;
    private final String date;
    private final String time;
    private final String modes;
    private final boolean arriveBy;
    private final boolean wheelchair;
    private VBox resultBox;
    private ScheduledExecutorService scheduler;
    private Button starButton;
    private final SaveableTrip currentTrip;

    public ItinerariesView(Stop fromStop, Stop toStop, String date, String time, String modes, boolean arriveBy, boolean wheelchair) {
        this.fromStop = fromStop;
        this.toStop = toStop;
        this.date = date;
        this.time = time;
        this.modes = modes;
        this.arriveBy = arriveBy;
        this.wheelchair = wheelchair;
        currentTrip = new SaveableTrip(fromStop, toStop);
    }

    /**
     * Létrehozza a teljes view-t ScrollPane-ben.
     *
     * @return a view UI eleme
     */
    public ScrollPane getView() {
        HistoryManager.addTripToHistory(currentTrip);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("main-screen");

        HBox titleBox = new HBox(16);
        Button backButton = createBackButton();
        titleBox.getChildren().add(backButton);

        Label title = new Label("Útvonalak");
        title.getStyleClass().add("heading");
        titleBox.getChildren().add(title);

        Region wideSeparator = new Region();
        HBox.setHgrow(wideSeparator, Priority.ALWAYS);
        titleBox.getChildren().add(wideSeparator);

        starButton = new Button();
        starButton.getStyleClass().add("favorites-button");
        starButton.getStyleClass().add("clickable");
        starButton.setVisible(false);
        addHoverScaleEffect(starButton, 1.15);

        // betöltésnél az e
        if (isFavoriteTrip.test(currentTrip)) starButton.getStyleClass().add("favorites-button-active");

        // Kedvencekhez adás
        starButton.setOnAction(this::starButtonHandle);
        titleBox.getChildren().add(starButton);

        root.getChildren().add(titleBox);

        Label subtitle = new Label(fromStop.getName() + " - " + toStop.getName());
        subtitle.getStyleClass().add("subheading");
        root.getChildren().add(subtitle);

        root.getChildren().add(createSeparator());

        resultBox = new VBox(12);
        root.getChildren().add(resultBox);

        Label loadingLabel = new Label("🔄 Útvonalak betöltése...");
        resultBox.getChildren().add(loadingLabel);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private String formatTime(long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    /**
     * Ez a metódus kezeli az egyes utazási lépések szép kiíratásához szükséges HBox elkészítését
     *
     * @param step egy konkrét útvonal lépés (séta vagy járat)
     * @return egy HBox vizuálisan megformázva
     */
    private HBox createStepBox(Step step) {
        // Az alap box, ami 3 részre van osztva
        // [stepTimeLabel] [stepCircle] [stepInfoBox]
        HBox stepBox = new HBox(8);
        stepBox.setAlignment(Pos.TOP_LEFT);

        // stepTimeLabel
        Label stepTimeLabel = new Label();
        stepTimeLabel.getStyleClass().add("timelabel");
        stepTimeLabel.setMinWidth(45);
        stepTimeLabel.prefWidth(45);
        stepTimeLabel.setMaxWidth(45);

        // KÖR
        Color dotColor = Color.valueOf(step instanceof RouteStep route ? route.route().getColor() : "#333333");
        Circle outerDot = new Circle(9);
        outerDot.setFill(dotColor);

        Circle innerDot = new Circle(4);
        innerDot.setFill(Color.WHITE);

        // Egymásra lesznek overlayelve
        StackPane dotStack = new StackPane(outerDot, innerDot);
        // Kell mert amúgy nem tudom középre igazitani
        VBox dotWrapper = new VBox(dotStack);

        // stepInfoBox, ami két részből áll
        // [placeLabel]
        // [routeStepBox]
        VBox stepInfoBox = new VBox(6);

        Label placeLabel = new Label();
        placeLabel.setText(step.fromStop().getName());
        placeLabel.getStyleClass().add("timelabel");

        HBox routeStepBox = new HBox(6);
        routeStepBox.getStyleClass().add("route-step");

        // Megvizsgáljuk instanceof-al hogy melyik örökölt osztály tagja
        if (step instanceof WalkStep walk)
        {
            placeLabel.setOnMouseClicked(null);
            stepTimeLabel.setText(formatTimeMs(walk.startTime()));

            Label walkLabel = new Label(walk.walkTime() + "p séta");
            routeStepBox.getChildren().addAll(
                    stepTimeLabel,
                    walkLabel
            );
        }
        // Ha nem séta, akkor public transportos lépés
        else if (step instanceof RouteStep route)
        {
            stepTimeLabel.setText(formatTimeMs(route.startTime()));
            Label routeLabel = new Label(route.route().getRouteShortName());

            // Kattintható, ha RouteStep
            routeStepBox.getStyleClass().add("clickable");
            placeLabel.getStyleClass().add("clickable");
            placeLabel.setOnMouseClicked(_ -> SceneManager.show(new StopView(step.fromStop(), date)));

            routeLabel.getStyleClass().add("route-badge");
            routeLabel.setStyle(
                "-fx-background-color: " + route.route().getColor() + ";" +
                "-fx-text-fill: " + route.route().getTextColor() + ";"
            );

            Label destinationLabel = new Label("▶ " + route.route().getHeadsign());
            destinationLabel.getStyleClass().add("destination");

            routeStepBox.getChildren().addAll(
                    stepTimeLabel,
                    routeLabel,
                    destinationLabel
            );
            // Ha nem séta, akkor kattintható
            routeStepBox.setOnMouseClicked(_ -> SceneManager.show(new TripView(route.tripId(), fromStop, toStop, date, route)));
        }

        stepInfoBox.getChildren().addAll(
                placeLabel,
                routeStepBox
        );

        stepBox.getChildren().addAll(
                stepTimeLabel,
                dotWrapper,
                stepInfoBox
        );

        return stepBox;
    }

    private String formatTimeMs(long epochMillis) {
        Instant instant = Instant.ofEpochMilli(epochMillis);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    /**
     * Indítja az automatikus frissítést háttérszálon.
     */
    @Override
    public void start() {
        System.out.println("ItinerariesView autómatikus frissítése elindult!");
        if (scheduler != null && !scheduler.isShutdown()) return;

        // Egy szálon futó időzített executor
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Method reference, gondoltam most ezt használom, az IDEA mindig feldobta a Lambda expressionnél helyettesítésnek.
        scheduler.scheduleAtFixedRate(this::refreshTask, 0, 2, TimeUnit.MINUTES);
    }

    /**
     * Frissíti az útvonalterveket és megjeleníti őket.
     */
    public void refreshTask() {
        // Anonim belső osztály
        Task<List<Itinerary>> task = new Task<>() {
            @Override
            protected List<Itinerary> call() throws Exception {
                return GetPlanTrip.getItineraries(fromStop, toStop, date, time, modes, arriveBy, wheelchair);
            }
        };
        task.setOnSucceeded(_ -> {
            System.out.println("ItinerariesView autómatikusan frissült!");
            resultBox.getChildren().clear();
            // Megkapjuk a taskban lefutó metódus visszatérési értékét
            List<Itinerary> itineraries = task.getValue();

            for (Itinerary it : itineraries) {
                VBox card = new VBox(8);
                card.getStyleClass().add("transport-modes-grid");
                Label timeLabel = new Label(
                        formatTime(it.startTime()) +
                                " ▶ " + formatTime(it.endTime()) +
                                " ( " + (it.duration() / 60) + " perc )"
                );
                timeLabel.getStyleClass().add("centeredsubheading");

                // VBoxban egymás alatt megjelennek a lépések
                VBox legsBox = new VBox(10);
                for (int i = 0; i < it.routeSummary().size(); i++) {
                    // Itt lesz dezájnos a lépés kinézet vagy idk minek hívjam
                    // Út darab/részlet amikből áll ez a fos
                    // Fel lesz darabolva és szépen HBoxban kiírva egymás mellett a járatszám, célállomás stb
                    HBox stepBox = createStepBox(it.routeSummary().get(i));
                    legsBox.getChildren().add(stepBox);

                    if (i < it.routeSummary().size())
                        legsBox.getChildren().add(createSeparator());
                }
                HBox endStepBox = new HBox(8);
                // stepTimeLabel
                Label stepTimeLabel = new Label(formatTime(it.endTime()));
                stepTimeLabel.getStyleClass().add("timelabel");
                stepTimeLabel.setMinWidth(45);
                stepTimeLabel.prefWidth(45);
                stepTimeLabel.setMaxWidth(45);

                // KÖR
                Color dotColor = Color.web(it.routeSummary().getLast() instanceof RouteStep route ? route.route().getColor() : "#333333");
                Circle outerDot = new Circle(9);
                outerDot.setFill(dotColor);

                Circle innerDot = new Circle(4);
                innerDot.setFill(Color.WHITE);

                // Egymásra lesznek overlayelve
                StackPane dotStack = new StackPane(outerDot, innerDot);
                // Kell mert amúgy nem tudom középre igazitani
                VBox dotWrapper = new VBox(dotStack);

                // endStopLabel
                Label endPlaceLabel = new Label();
                endPlaceLabel.setText(it.to().getName());
                endPlaceLabel.setOnMouseClicked(_ -> SceneManager.show(new StopView(it.to(), date)));
                endPlaceLabel.getStyleClass().add("timelabel");
                endPlaceLabel.getStyleClass().add("clickable");

                endStepBox.getChildren().addAll(
                        stepTimeLabel,
                        dotWrapper,
                        endPlaceLabel);


                Label walkLabel = new Label((int) it.totalWalkDistance() + " m séta");
                walkLabel.getStyleClass().add("centeredsubheading");

                HBox buttonsBox = new HBox(8);
                Button printButton = new Button("Mentés Fájlba");
                printButton.getStyleClass().add("search-button");
                addHoverScaleEffect(printButton, 1.05);

                printButton.setOnAction(_ -> {
                    try {
                        // Felépítjük a tervet
                        StringBuilder content = new StringBuilder();
                        content.append("Útvonal: ").append(fromStop.getName())
                                .append(" ➔ ").append(toStop.getName()).append("\n");
                        content.append("Indulás: ").append(formatTime(it.startTime()))
                                .append(" - Érkezés: ").append(formatTime(it.endTime())).append("\n");
                        content.append("Időtartam: ").append(it.duration() / 60).append(" perc\n");
                        content.append("Séta: ").append((int) it.totalWalkDistance()).append(" m\n\n");

                        content.append("Lépések:\n");
                        for (Step step : it.routeSummary()) {
                            if (step instanceof WalkStep walk) {
                                content.append("  [Séta] ")
                                        .append(formatTimeMs(walk.startTime()))
                                        .append(" • ")
                                        .append(walk.fromStop().getName())
                                        .append(": ").append(walk.walkTime()).append("p séta")
                                        .append("\n");
                            } else if (step instanceof RouteStep route) {
                                content.append("  [Járat] ")
                                        .append(formatTimeMs(route.startTime()))
                                        .append(" • ")
                                        .append(route.fromStop().getName())
                                        .append(": ")
                                        .append(route.route().getRouteShortName())
                                        .append(" ➔ ").append(route.route().getHeadsign())
                                        .append("\n");
                            }
                        }


                        content.append("\nÉrkezési hely: ").append(it.to().getName()).append("\n");

                        // Fájl létrehozás
                        String fileName = "utvonal.txt";
                        // Szebb mint egy FileWriter
                        java.nio.file.Files.writeString(java.nio.file.Path.of(fileName), content.toString());
                        System.out.println("Fájl mentve: " + fileName);
                    } catch (Exception exception) {
                        System.out.println("Hiba: " + exception.getMessage());
                    }
                });

                buttonsBox.getChildren().addAll(printButton);
                buttonsBox.setAlignment(Pos.CENTER_LEFT);

                card.getChildren().addAll(timeLabel, legsBox, endStepBox, walkLabel, buttonsBox);
                resultBox.getChildren().add(card);
                starButton.setVisible(true);
            }
        });
        task.setOnFailed(_ -> {
            System.out.println("Hiba történt: " + task.getException().getMessage());

            resultBox.getChildren().clear();
            VBox card = new VBox(8);
            card.getStyleClass().add("transport-modes-grid");
            Label label = new Label("Nincs útvonal a kiválasztott megálókhoz.");
            card.getChildren().add(label);

            resultBox.getChildren().add(card);
            stop();
        });
        new Thread(task).start();
    }

    /**
     * Leállítja az időzített frissítést.
     */
    @Override
    public void stop() {
        System.out.println("ItinerariesView autómatikus frissítése leállt!");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    /**
     * Váltja a kedvenc állapotot a csillag ikon lenyomásakor.
     *
     * @param e az esemény (nem használt)
     */
    private void starButtonHandle(ActionEvent e) {
        toggleFavoriteTrip(currentTrip, starButton);
    }
}