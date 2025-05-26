package jfutar.views.details;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import jfutar.managers.SceneManager;
import jfutar.records.itinerary.RouteStep;
import jfutar.records.stop.Stop;
import jfutar.records.stop.StopTime;
import jfutar.records.trip.Trip;
import jfutar.requests.GetTripDetails;
import jfutar.views.AbstractView;

import static jfutar.managers.UtilityManager.*;

/**
 * A TripView egy konkrét menet (trip) teljes útvonalát jeleníti meg megállókkal és időpontokkal.
 * A nézet automatikusan lekéri a megállókat és kirajzolja őket.
 */
public final class TripView extends AbstractView {

    String tripId;
    Stop fromStop;
    Stop toStop;
    String date;
    private final RouteStep route;

    public TripView(String tripId, Stop fromStop, Stop toStop, String date, RouteStep route) {
        this.tripId = tripId;
        this.fromStop = fromStop;
        this.toStop = toStop;
        this.date = date;
        this.route = route;
    }

    /**
     * Létrehozza a teljes view-t ScrollPane-ben.
     *
     * @return a nézet UI eleme
     */
    public ScrollPane getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("main-screen");

        HBox titleBox = new HBox(10);

        Button backButton = createBackButton();
        titleBox.getChildren().add(backButton);

        Label title = new Label("Menet");
        title.getStyleClass().add("heading");
        titleBox.getChildren().add(title);

        root.getChildren().addAll(titleBox, createRouteBox(), createSeparator());

        VBox stopsBox = new VBox(12);
        stopsBox.getStyleClass().add("transport-modes-grid");

        Label loadingLabel = new Label("🔄 Megállók betöltése...");
        stopsBox.getChildren().add(loadingLabel);

        root.getChildren().add(stopsBox);

        // Betöltés egy Task-kal, hogy ne fagyjon be a főszál
        Task<Trip> task = getTripTask(stopsBox);
        new Thread(task).start();

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    /**
     * Járat részleteinek lekérését végző háttérfolyamat létrehozása.
     *
     * @param stopsBox a VBox, ahová majd a megállók kerülnek
     * @return a futtatandó Task
     */
    private Task<Trip> getTripTask(VBox stopsBox) {
        Task<Trip> task = new Task<>() {
            @Override
            protected Trip call() throws Exception {
                return GetTripDetails.getTripDetails(tripId, fromStop, toStop, date);
            }
        };
        task.setOnSucceeded(_ -> {
            Trip trip = task.getValue();
            stopsBox.getChildren().clear();

            for (StopTime st : trip.stops())
                stopsBox.getChildren().add(createStopRow(st));
        });
        task.setOnFailed(_ -> {
            System.out.println("Hiba történt: " + task.getException().getMessage());
            stopsBox.getChildren().clear();
            stopsBox.getChildren().add(new Label("Hiba történt a megállók lekérésekor."));
        });
        return task;
    }

    /**
     * Egy megállósort hoz létre a megálló nevével és időpontjával.
     *
     * @param st a megállási adat
     * @return a megjelenítendő HBox
     */
    private HBox createStopRow(StopTime st) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        String time = formatTime(st.departureTime());

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("timelabel");
        timeLabel.getStyleClass().add(st.departureTime() < (System.currentTimeMillis() / 1000) ? "timelabel-obsolete" : "timelabel");

        timeLabel.setMinWidth(60);

        //KÖR
        Circle outerDot = new Circle(9);
        outerDot.setFill(Color.BLACK);

        Circle innerDot = new Circle(4);
        innerDot.setFill(Color.WHITE);

        // Egymásra lesznek overlayelve
        StackPane dotStack = new StackPane(outerDot, innerDot);
        // Kell mert amúgy nem tudom középre igazitani
        VBox dotWrapper = new VBox(dotStack);

        Label stopLabel = new Label(st.stop().getName());
        stopLabel.getStyleClass().add("destination");
        stopLabel.getStyleClass().add(st.departureTime() < (System.currentTimeMillis() / 1000) ? "timelabel-obsolete" : "timelabel");

        row.getChildren().addAll(timeLabel, dotWrapper, stopLabel);
        row.setOnMouseClicked(_ -> SceneManager.show(new StopView(st.stop(), date)));
        row.getStyleClass().add("clickable");

        return row;
    }

    /**
     * Megjeleníti az útvonal fejlécet (járat badge + irány).
     *
     * @return HBox komponens az útvonalról
     */
    private HBox createRouteBox() {
        HBox routeStepBox = new HBox(6);
        routeStepBox.getStyleClass().add("route-step");
        routeStepBox.getStyleClass().add("clickable");

        Label routeLabel = new Label(route.route().getRouteShortName());
        routeLabel.getStyleClass().add("route-badge");
        routeLabel.setStyle(
                "-fx-background-color: " + route.route().getColor() + ";" +
                        "-fx-text-fill: " + route.route().getTextColor() + ";"
        );

        Label destinationLabel = new Label("▶ " + route.route().getHeadsign());
        destinationLabel.getStyleClass().add("destination");

        routeStepBox.getChildren().addAll(routeLabel, destinationLabel);

        return routeStepBox;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
