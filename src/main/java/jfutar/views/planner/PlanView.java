package jfutar.views.planner;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import jfutar.managers.SceneManager;
import jfutar.records.stop.Stop;
import jfutar.views.AbstractView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

import javafx.animation.RotateTransition;
import javafx.util.Duration;

import static jfutar.managers.UtilityManager.*;

/**
 * A kezdő view, amely lehetővé teszi a felhasználó számára, hogy megadja
 * a kiindulási és cél megállót, valamint az időpontot és közlekedési módokat.
 * Innen indítható keresés akár konkrét megállókkal, vagy kedvencekből.
 */
public final class PlanView extends AbstractView {

    private static Stop FROM_STOP;
    private static Stop TO_STOP;

    private Label fromValue;
    private Label toValue;

    private final Map<String, ToggleButton> transportModes = new HashMap<>();

    private static String TIME_MODE;
    private ComboBox<String> timeModeCombo;

    private static String DATE;
    private static String TIME;

    private TextField dateField;
    private TextField timeField;

    private static boolean accessibleOnly = false;

    /**
     * Felépíti az útvonaltervező view-t.
     *
     * @return a view UI eleme
     */
    public Parent getView() {
        StackPane mainPane = new StackPane();
        mainPane.setId("plan-view");
        mainPane.setAlignment(Pos.BOTTOM_CENTER);

        VBox backgroundColor = new VBox();
        backgroundColor.setPrefWidth(Double.MAX_VALUE);
        backgroundColor.setPrefHeight(Double.MAX_VALUE);
        backgroundColor.setStyle(
                "-fx-background-color: #181922;"
        );

        // Háttérkép betöltése
        Image bgImage = new Image(Objects.requireNonNull(getClass().getResource("/img/background.png")).toExternalForm());
        ImageView background = new ImageView(bgImage);
        background.setPreserveRatio(true);
        background.setFitWidth(400);
        background.setSmooth(true);
        StackPane.setMargin(background, new Insets(-8, 0, 0, 0));

        // Igazítás a VBox tetejéhez
        StackPane.setAlignment(background, Pos.TOP_CENTER);

        VBox mainContent = new VBox(16);
        mainContent.setPadding(new Insets(24));
        mainContent.getStyleClass().add("root");
        StackPane.setMargin(mainContent, new Insets(140, 0, 0, 0));
        mainContent.setStyle("-fx-background-radius: 30 30 0 0");

        // Error Label ami majd belebeg mert menő
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setTranslateY(-80); // kezdő pozíció
        StackPane.setAlignment(errorLabel, Pos.TOP_CENTER);
        StackPane.setMargin(errorLabel, new Insets(20, 20, 0, 20));

        mainPane.getChildren().addAll(backgroundColor, background, mainContent, errorLabel);

        // Fő title
        HBox titleBox = new HBox();
        Label title = new Label("Útvonaltervezés");
        title.getStyleClass().add("heading");
        titleBox.getChildren().add(title);

        Region wideSeparator = new Region();
        HBox.setHgrow(wideSeparator, Priority.ALWAYS);
        titleBox.getChildren().add(wideSeparator);

        Button searchbutton;
        Button historyButton;

        StackPane stopSelectPane = new StackPane();
        stopSelectPane.setMaxWidth(400);
        stopSelectPane.setPrefWidth(400);
        stopSelectPane.setMinWidth(75);

        VBox stopSelectBox = new VBox(15);
        // Honnan mező
        Label fromTitle = new Label("Honnan?");
        fromTitle.getStyleClass().add("subheading");

        fromValue = new Label(FROM_STOP != null ? FROM_STOP.getName() : "Kattints a kereséshez");

        VBox fromBox = new VBox(4, fromTitle, fromValue);

        // Itt kerül használatra a Consumer típusú metódusom
        fromBox.setOnMouseClicked(_ -> SceneManager.show(new StopSearchView(fromStopSelected)));
        fromBox.getStyleClass().add("search-field");
        fromBox.getStyleClass().add("clickable");
        addHoverScaleEffect(fromBox, 1.05);

        // Hova mező
        Label toTitle = new Label("Hová?");
        toTitle.getStyleClass().add("subheading");

        toValue = new Label(TO_STOP != null ? TO_STOP.getName() : "Kattints a kereséshez");

        VBox toBox = new VBox(4, toTitle, toValue);
        toBox.setOnMouseClicked(_ -> SceneManager.show(new StopSearchView(toStopSelected)));

        toBox.getStyleClass().add("search-field");
        toBox.getStyleClass().add("clickable");
        addHoverScaleEffect(toBox, 1.05);

        // Csere gomb
        Button swapButton = new Button();
        addHoverScaleEffect(swapButton, 1.1);
        swapButton.setOnAction(_ -> {
            RotateTransition rotate = new RotateTransition(Duration.millis(250), swapButton);
            rotate.setByAngle(-180);
            rotate.setCycleCount(1);
            rotate.play();

            // Cseréljük a szöveget
            String tempText = fromValue.getText();
            fromValue.setText(toValue.getText());
            toValue.setText(tempText);

            // Cseréljük a megálló objektumokat is
            Stop tempStop = FROM_STOP;
            FROM_STOP = TO_STOP;
            TO_STOP = tempStop;
        });
        swapButton.getStyleClass().addAll("circle-button-big", "clickable");
        SVGPath icon = new SVGPath();
        icon.setContent("M14 16H19V21M10 8H5V3M19.4176 9.0034C18.8569 7.61566 17.9181 6.41304 16.708 5.53223C15.4979 4.65141 14.0652 4.12752 12.5723 4.02051C11.0794 3.9135 9.58606 4.2274 8.2627 4.92661C6.93933 5.62582 5.83882 6.68254 5.08594 7.97612M4.58203 14.9971C5.14272 16.3848 6.08146 17.5874 7.29157 18.4682C8.50169 19.3491 9.93588 19.8723 11.4288 19.9793C12.9217 20.0863 14.4138 19.7725 15.7371 19.0732C17.0605 18.374 18.1603 17.3175 18.9131 16.0239");
        icon.setStroke(Color.WHITE);
        icon.setStrokeWidth(3);
        swapButton.setGraphic(icon);

        stopSelectBox.getChildren().addAll(fromBox, toBox);
        stopSelectPane.getChildren().addAll(stopSelectBox, swapButton);

        // Idő beállítások HBox-ban
        FlowPane timePane = new FlowPane();
        timePane.setHgap(8);
        timePane.setVgap(8);
        timePane.setAlignment(Pos.CENTER_LEFT);

        // Idő mód választó
        timeModeCombo = new ComboBox<>();
        timeModeCombo.getItems().addAll("Indulás most", "Indulási idő", "Érkezési idő");
        timeModeCombo.setValue("Indulás most");
        timeModeCombo.setPromptText("Idő mód");
        timeModeCombo.getStyleClass().add("combo-box");

        if (TIME_MODE != null)
            timeModeCombo.setValue(TIME_MODE);

        // Listener hogy elmentse a valuet
        timeModeCombo.valueProperty().addListener((_, _, newVal) -> {
            System.out.println(newVal);
            TIME_MODE = newVal;
        });

        // Dátum (TextField)
        dateField = new TextField();
        dateField.setPromptText(LocalDate.now().toString());
        dateField.getStyleClass().add("search-field");
        // If kicserélve erre
        dateField.setText(Objects.requireNonNullElseGet(DATE, () -> LocalDate.now().toString()));

        dateField.textProperty().addListener((_, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,4}-?\\d{0,2}-?\\d{0,2}")) {
                dateField.setText(oldValue);
            }
            else
                DATE = newValue;
        });

        // Idő
        timeField = new TextField();
        timeField.setPromptText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeField.getStyleClass().add("search-field");
        // If kicserélve erre
        timeField.setText(Objects.requireNonNullElseGet(TIME, () -> LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))));

        timeField.textProperty().addListener((_, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,2}:?\\d{0,2}")) {
                timeField.setText(oldValue);
            }
            else
                TIME = newValue;
        });

        HBox dateBox = new HBox(8);
        dateBox.getChildren().addAll(
                dateField,
                timeField
        );

        // HBox-hoz adás
        timePane.getChildren().addAll(timeModeCombo, dateBox);
        // Szélesség arányok finomhangolása
        timeModeCombo.setMinWidth(150);
        timeModeCombo.setMaxWidth(150);
        dateField.setMinWidth(125);
        timeField.setMinWidth(75);
        dateField.setMaxWidth(125);
        timeField.setMaxWidth(75);

        // Közlekedési módok
        Label modeLabel = new Label("Közlekedési módok");
        modeLabel.getStyleClass().add("subheading");
        FlowPane modesGrid = new FlowPane(16,10);
        modesGrid.setMaxWidth(400);
        modesGrid.setMinWidth(Double.MIN_VALUE);

        modesGrid.setPadding(new Insets(10));
        modesGrid.getStyleClass().add("transport-modes-grid");

        // Hardcodeolva van, mivel még nem találtam rá api lekérést
        Task<Void> loadIconsTask = getLoadIconsTask(modesGrid);

        new Thread(loadIconsTask).start();

        // További beállítások
        ToggleButton accessibleButton = new ToggleButton("Akadálymentes járatok előnyben részesítése");
        accessibleButton.setPrefWidth(400);
        accessibleButton.setMaxWidth(400);
        accessibleButton.setMinHeight(50);
        accessibleButton.getStyleClass().add("accessibility-button");

        if (accessibleOnly)
            accessibleButton.setSelected(true);
        if (!accessibleOnly)
            accessibleButton.setSelected(false);

        addHoverScaleEffect(accessibleButton, 1.05);

        accessibleButton.setOnAction(_ -> {
            accessibleOnly = accessibleButton.isSelected();
            System.out.println("Akadálymentes keresés: " + accessibleOnly);
        });

        historyButton = createHistoryButton(dateField.getText().trim(),
                timeField.getText().trim(),
                buildSelectedModes(),
                timeModeCombo.getValue().equals("Érkezési idő"),
                accessibleButton.isSelected()
        );
        titleBox.getChildren().add(historyButton);

        Region smallSeparator = new Region();
        HBox.setMargin(smallSeparator, new Insets(0, 8, 0, 0));
        titleBox.getChildren().add(smallSeparator);

        searchbutton = createSearchButton(dateField.getText().replace("-", ""));
        titleBox.getChildren().add(searchbutton);

        // Keresés gomb
        Button searchRouteButton = new Button("Útvonal keresése");
        searchRouteButton.getStyleClass().add("search-button-big-green");
        searchRouteButton.setMaxWidth(400);
        searchRouteButton.setPrefWidth(400);
        searchRouteButton.setMinWidth(75);
        addHoverScaleEffect(searchRouteButton, 1.05);

        searchRouteButton.setOnAction(_ -> {
            if (FROM_STOP == null || TO_STOP == null)
            {
                showError(errorLabel, "Mindkét megállót ki kell választani!");
                return;
            }

            String date = dateField.getText().trim();
            String time = timeField.getText().trim();
            boolean arriveBy = timeModeCombo.getValue().equals("Érkezési idő");
            boolean wheelchair = accessibleButton.isSelected();

            if (timeModeCombo.getValue().equals("Indulás most"))
            {
                date = LocalDate.now().toString();
                time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            }
            else if (date.isEmpty() || time.isEmpty())
            {
                showError(errorLabel, "Dátum és idő kitöltése kötelező!");
                return;
            }

            SceneManager.show(new ItinerariesView(FROM_STOP, TO_STOP, date.replace("-",""), time, buildSelectedModes(), arriveBy, wheelchair));
            System.out.println("Dátum: " + date);
        });

        HBox favoritesBox = new HBox(10);
        favoritesBox.setPrefWidth(400); // fix szélesség
        favoritesBox.setMaxWidth(400);

        Button favoriteStopsButton = new Button("Kedvenc Megállók");
        addHoverScaleEffect(favoriteStopsButton, 1.05);
        Button favoriteTripsButton = new Button("Kedvenc Útak");
        addHoverScaleEffect(favoriteTripsButton, 1.05);

        // Egyforma fix szélesség, mert sehogy sem akart a szöveg miatt mukodni
        double buttonWidth = (400 - 10) / 2.0; // 10 a spacing
        favoriteStopsButton.setPrefWidth(buttonWidth);
        favoriteTripsButton.setPrefWidth(buttonWidth);

        favoriteStopsButton.getStyleClass().add("search-button");
        favoriteTripsButton.getStyleClass().add("search-button");

        favoritesBox.getChildren().addAll(favoriteStopsButton, favoriteTripsButton);

        favoriteTripsButton.setOnAction(_ -> {
            String date = dateField.getText().trim();
            String time = timeField.getText().trim();
            boolean arriveBy = timeModeCombo.getValue().equals("Érkezési idő");
            boolean wheelchair = accessibleButton.isSelected();

            if (timeModeCombo.getValue().equals("Indulás most"))
                time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            else if (date.isEmpty() || time.isEmpty())
            {
                showError(errorLabel, "Dátum és idő kitöltése kötelező!");
                return;
            }

            SceneManager.show(new FavoriteTripsView(dateField.getText().trim(), time, buildSelectedModes(), arriveBy, wheelchair));
        });

        favoriteStopsButton.setOnAction(_ -> SceneManager.show(new FavoriteStopsView()));

        mainContent.getChildren().addAll(
                titleBox,
                stopSelectPane,
                timePane,
                modeLabel, modesGrid,
                accessibleButton, searchRouteButton,
                favoritesBox
        );

        // -----------------------------------------------------------------------------------

        /*
        Label historyLabel = new Label("Legutóbbi útvonalak");
        historyLabel.getStyleClass().add("subheading");

        VBox historyBox = new VBox(8);
        historyBox.getStyleClass().add("transport-modes-grid");

        List<SaveableTrip> recentTrips = HistoryManager.getRecentTrips();

        if (recentTrips.isEmpty()) {
            Label emptyLabel = new Label("Nincsenek korábbi útvonalak.");
            historyBox.getChildren().add(emptyLabel);
        } else {
            for (int i = 0; i < recentTrips.size(); i++) {
                SaveableTrip trip = recentTrips.get(i);

                HBox tripBox = new HBox();
                tripBox.getStyleClass().add("route-step");
                tripBox.setAlignment(Pos.CENTER_LEFT);
                tripBox.setSpacing(8);

                Label nameLabel = new Label(trip.getFrom().getName() + " - " + trip.getTo().getName());
                nameLabel.getStyleClass().add("timelabel");
                nameLabel.getStyleClass().add("clickable");
                nameLabel.setOnMouseClicked(_ -> SceneManager.show(new ItinerariesView(
                        trip.getFrom(),
                        trip.getTo(),
                        dateField.getText().trim().replace("-",""),
                        timeField.getText().trim(),
                        buildSelectedModes(),
                        timeModeCombo.getValue().equals("Érkezési idő"),
                        accessibleOnly
                )));

                tripBox.getChildren().add(nameLabel);
                historyBox.getChildren().add(tripBox);

                if (i < recentTrips.size() - 1)
                    historyBox.getChildren().add(new Separator());
            }
        }

        mainContent.getChildren().addAll(historyLabel, historyBox);*/


        return mainPane;
    }

    /**
     * Consumer<Stop> típusú metódus ami kihasználja a Consumer adta lehetőségeket.
     * .accept segítségével átadásra kerül a kiválasztott megálló a másik View-ből.
     */
    private final Consumer<Stop> fromStopSelected = new Consumer<>() {
        @Override
        public void accept(Stop stop) {
            FROM_STOP = stop;
            fromValue.setText(stop.getName());
        }
    };
    /**
     * Consumer<Stop> típusú metódus ami kihasználja a Consumer adta lehetőségeket.
     * .accept segítségével átadásra kerül a kiválasztott megálló a másik View-ből.
     */
    private final Consumer<Stop> toStopSelected = new Consumer<>() {
        @Override
        public void accept(Stop stop) {
            TO_STOP = stop;
            toValue.setText(stop.getName());
        }
    };

    /**
     * Háttérben betölti az ikonokat a közlekedési módokhoz.
     *
     * @param modesGrid ahova az ikonokat hozzáadja
     * @return Task a betöltéshez
     */
    private Task<Void> getLoadIconsTask(FlowPane modesGrid) {
        String[] apiModes = {"SUBWAY", "SUBURBAN_RAILWAY", "FERRY", "TRAM", "TROLLEYBUS", "BUS", "RAIL", "COACH"};
        String[] modeIconNames = {"subway", "suburban-railway", "ferry", "tram", "trolleybus", "bus", "rail", "bus"};
        String[] modeColors = {"000000", "000000", "000000", "FFD400", "E4231B", "009EE3", "2E5EA8", "F9AB13"};
        String[] modeSecondaryColors = {"FFFFFF", "FFFFFF", "FFFFFF", "000000", "FFFFFF", "FFFFFF", "FFFFFF", "000000"};

        // Ikonok háttérben betöltése Task-kal
        return new Task<>() {
            @Override
            protected Void call() {
                for (int i = 0; i < apiModes.length; i++) {
                    final String mode = apiModes[i];

                    final String baseUrl = "https://futar.bkk.hu/api/ui-service/v1/icon?name=" + modeIconNames[i] + "&color=" + modeColors[i] + "&secondaryColor=" + modeSecondaryColors[i];
                    final String greyUrl = "https://futar.bkk.hu/api/ui-service/v1/icon?name=" + modeIconNames[i] + "&color=C3C3C3&secondaryColor=FFFFFF";

                    Platform.runLater(() -> {
                        ToggleButton toggle = new ToggleButton();
                        toggle.setSelected(true);
                        toggle.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

                        ImageView imageView = new ImageView();
                        imageView.setFitWidth(50);
                        imageView.setFitHeight(50);
                        imageView.setPreserveRatio(true);
                        imageView.getStyleClass().add("clickable");

                        StackPane wrapper = new StackPane(imageView);
                        addHoverScaleEffect(wrapper, 1.15);

                        wrapper.setAlignment(Pos.CENTER);

                        toggle.selectedProperty().addListener((_, _, isNowSelected) -> {
                            String imageUrl = isNowSelected ? baseUrl : greyUrl;
                            imageView.setImage(new Image(imageUrl, true));
                        });

                        // Betöltés színesként
                        imageView.setImage(new Image(baseUrl, true));

                        wrapper.setOnMouseClicked(_ -> toggle.setSelected(!toggle.isSelected()));

                        transportModes.put(mode, toggle);
                        modesGrid.getChildren().add(wrapper);
                    });
                }
                return null;
            }
        };
    }

    @Override
    public void start() {
        System.out.println("Start");

        // Betöltjük a cuccokat hogy ne kelljen megint kiválasztani
        if (FROM_STOP != null && fromValue != null)
            fromValue.setText(FROM_STOP.getName());

        if (TO_STOP != null && toValue != null)
            toValue.setText(TO_STOP.getName());

        if (TIME_MODE != null)
            timeModeCombo.setValue(TIME_MODE);

        if (DATE != null)
            dateField.setText(DATE);

        if (TIME != null)
            timeField.setText(TIME);
    }

    @Override
    public void stop() {
        System.out.println("Stop");
    }

    /**
     * Kiír egy error-t.
     *
     * @param label a cél Label
     * @param message az üzenet
     */
    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        // Quick Doc-ból kivágva:
        // Defines whether or not this node's layout will be managed by its parent.
        label.setManaged(true);

        // Induló pozíció (fentről lebeg be)
        label.setTranslateY(-80);

        // Lefelé belebeg
        TranslateTransition show = new TranslateTransition(Duration.millis(300), label);
        show.setToY(0);
        show.play();

        // Időzítve va
        Timer timer = new Timer();
        // Anonim belső osztály
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    // Vissza is kellene menniexd
                    TranslateTransition hide = new TranslateTransition(Duration.millis(300), label);
                    hide.setToY(-80);
                    hide.setOnFinished(_ -> {
                        label.setText("");
                        label.setVisible(false);
                        label.setManaged(false);
                    });
                    hide.play();
                });
            }
            // 5mp delayyel
        }, 3000);
    }

    private String buildSelectedModes() {
        List<String> selectedModes = new ArrayList<>();
        selectedModes.add("WALK");
        transportModes.forEach((mode, toggle) -> {
            if (toggle.isSelected()) selectedModes.add(mode);
        });
        return String.join(",", selectedModes);
    }
}