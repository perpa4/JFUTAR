package jfutar.views.details;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import jfutar.managers.HistoryManager;
import jfutar.managers.SceneManager;
import jfutar.records.itinerary.RouteStep;
import jfutar.records.stop.Departure;
import jfutar.records.stop.Stop;
import jfutar.records.stop.StopInfo;
import jfutar.records.trip.Route;
import jfutar.requests.GetArrivalsAndDeparturesForStop;
import jfutar.views.AbstractView;
import java.util.List;
import java.util.concurrent.*;

import static jfutar.managers.UtilityManager.*;

/**
 * A StopView osztály felelős egy-egy megálló megjelenítéséért.
 * Megjeleníti az innen induló járatokat, illetve néhány közeli indulást kiír.
 * Az indulások frissülnek percenként.
 */
public final class StopView extends AbstractView {
    private final Stop stop;
    private final String date;
    private FlowPane routesBox;
    private VBox departuresBox;
    private ScheduledExecutorService scheduler;

    private Button starButton;

    public StopView(Stop stop, String date) {
        this.stop = stop;
        this.date = date;
    }

    /**
     * Létrehozza a teljes view-t ScrollPane-ben.
     *
     * @return a view UI eleme
     */
    public ScrollPane getView() {
        HistoryManager.addStopToHistory(stop);

        // --------------------------------------------[UI Elemek]-------------------------------------------
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("main-screen");

        HBox titleBox = new HBox(10);
        Button backButton = createBackButton();
        titleBox.getChildren().add(backButton);

        Label title = new Label("Indulások");
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
        if (isFavoriteStop.test(stop)) starButton.getStyleClass().add("favorites-button-active");

        // Kedvencekhez adás - method reference handle-el
        starButton.setOnAction(this::starButtonHandle);

        titleBox.getChildren().add(starButton);

        root.getChildren().add(titleBox);

        Label subtitle = new Label(stop.getName());
        subtitle.getStyleClass().add("subheading");
        root.getChildren().add(subtitle);

        // Innen induló járatokat itt jeleníti meg
        routesBox = new FlowPane(7, 6);
        routesBox.getStyleClass().add("transport-modes-grid");

        root.getChildren().add(createSeparator());
        root.getChildren().add(routesBox);
        root.getChildren().add(createSeparator());

        // Ez meg a konkrét indulásokat tárolja
        departuresBox = new VBox(12);
        root.getChildren().add(departuresBox);
        departuresBox.getStyleClass().add("transport-modes-grid");

        Label loadingLabel = new Label("🔄 Indulások betöltése...");
        departuresBox.getChildren().add(loadingLabel);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    /**
     * Létrehozza a járat dobozt egy indulással.
     *
     * @param departure az indulás adatai
     * @return egy HBox, amely az indulás megjelenítéséért felel
     */
    private HBox createDepartureBox(Departure departure) {
        HBox departureBox = new HBox(6);
        departureBox.getStyleClass().add("route-step");
        departureBox.getStyleClass().add("clickable");

        Label departureTimeLabel = new Label();
        departureTimeLabel.setText(
                                    (departure.departureTime() - System.currentTimeMillis() / 1000) / 60 <= 0 ?
                                    "Most" :
                                    (departure.departureTime() - System.currentTimeMillis() / 1000) / 60 + "p"
        );
        departureTimeLabel.getStyleClass().add("timelabel");
        departureTimeLabel.setMinWidth(45);
        departureTimeLabel.prefWidth(45);
        departureTimeLabel.setMaxWidth(45);

        Label routeLabel = new Label(departure.route().getRouteShortName());
        routeLabel.getStyleClass().add("route-badge");
        routeLabel.setStyle(
                "-fx-background-color: " + departure.route().getColor() + ";" +
                        "-fx-text-fill: " + departure.route().getTextColor() + ";"
        );

        Label destinationLabel = new Label("▶ " + departure.route().getHeadsign());
        destinationLabel.getStyleClass().add("destination");

        departureBox.getChildren().addAll(
                departureTimeLabel,
                routeLabel,
                destinationLabel
        );

        departureBox.setOnMouseClicked(_ -> {
            RouteStep routeStep = new RouteStep(
                    new Route(
                            departure.route().getMode(),
                            departure.route().getRouteId(),
                            departure.route().getRouteShortName(),
                            departure.route().getHeadsign(),
                            departure.route().getColor(),
                            departure.route().getTextColor()),
                    departure.tripId(),
                    departure.departureTime() * 1000, // startTime
                    departure.departureTime() * 1000, // endTime
                    stop
            );

            SceneManager.show(new TripView(
                    departure.tripId(),
                    stop,
                    new Stop("?", "?", 0, 0),
                    date,
                    routeStep
            ));
        });

        return departureBox;
    }

    /**
     * Elindítja az automatikus frissítést (1 percenként).
     * ScheduledExecutorService-t használ. Köszönöm a 10. órát Dani :)
     */
    @Override
    public void start() {
        System.out.println("StopView autómatikus frissítése elindult!");
        if (scheduler != null && !scheduler.isShutdown()) return;

        // Egy szálon futó időzített executor
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Method reference, gondoltam most ezt használom, az IDEA mindig feldobta a Lambda expressionnél helyettesítésnek.
        scheduler.scheduleAtFixedRate(this::refreshTask, 0, 1, TimeUnit.MINUTES);
    }

    /**
     * Háttérben lekéri az indulásokat és frissíti a képernyőt.
     */
    public void refreshTask() {
        // Anonim belső osztály
        Task<StopInfo> task = new Task<>() {
            @Override
            protected StopInfo call() throws Exception {
                return GetArrivalsAndDeparturesForStop.getDeparturesForStop(stop.getId());
            }
        };

        task.setOnSucceeded(_ -> {
            System.out.println("StopView autómatikusan frissült!");
            StopInfo stopInfo = task.getValue();
            List<Departure> departures = stopInfo.departures();
            List<Route> routes = stopInfo.routes();

            routesBox.getChildren().clear();
            departuresBox.getChildren().clear();

            if (departures.isEmpty()) departuresBox.getChildren().add(new Label("Nincs közelgő indulás."));

            else {
                for (Route route : routes) {
                    Label routeLabel = new Label(route.getRouteShortName());
                    routeLabel.getStyleClass().add("route-badge");
                    routeLabel.setStyle(
                            "-fx-background-color: " + route.getColor() + ";" +
                                    "-fx-text-fill: " + route.getTextColor() + ";"
                    );
                    routesBox.getChildren().add(routeLabel);
                }

                for (int i = 0; i < departures.size(); i++) {
                    Departure departure = departures.get(i);
                    HBox departureBox = createDepartureBox(departure);
                    departuresBox.getChildren().add(departureBox);
                    if (i < departures.size() - 1) {
                        departuresBox.getChildren().add(createSeparator());
                    }
                }
            }
            starButton.setVisible(true);
        });

        task.setOnFailed(_ -> {
            departuresBox.getChildren().clear();
            departuresBox.getChildren().add(new Label("Nincs ilyen megálló."));
            stop();
        });

        new Thread(task).start();
    }

    /**
     * Leállítja a háttérfrissítést.
     */
    @Override
    public void stop() {
        System.out.println("StopView autómatikus frissítése leállt!");
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
        toggleFavoriteStop(stop, starButton);
    }
}
