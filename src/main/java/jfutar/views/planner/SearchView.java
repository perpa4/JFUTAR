package jfutar.views.planner;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import jfutar.managers.SceneManager;
import jfutar.records.stop.Stop;
import jfutar.records.trip.Route;
import jfutar.requests.GetGeocode;
import jfutar.views.AbstractView;
import jfutar.views.details.StopView;

import java.util.List;

import static jfutar.managers.UtilityManager.addHoverScaleEffect;
import static jfutar.managers.UtilityManager.createBackButton;

/**
 * Általános kereső view megállókra és járatokra.
 * Az eredmények megjelennek a szűrt keresési kifejezés alapján.
 */
public final class SearchView extends AbstractView {
    private final String date;
    public SearchView(String date) {
        this.date = date;
    }

    /**
     * Létrehozza a teljes view-t ScrollPane-ben.
     *
     * @return a view UI eleme
     */
    public ScrollPane getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        HBox titleBox = new HBox(10);
        Button backButton = createBackButton();
        titleBox.getChildren().add(backButton);

        Label title = new Label("Keresés");
        title.getStyleClass().add("heading");
        titleBox.getChildren().add(title);
        root.getChildren().add(titleBox);

        TextField searchField = new TextField();
        searchField.setPromptText("Keresés járatokra, megállókra és címekre");
        searchField.getStyleClass().add("search-field");

        VBox resultsBox = new VBox(8);

        searchField.textProperty().addListener((_, _, newVal) -> new Thread(() -> {
            try {
                List<Stop> stopResults = GetGeocode.getGeocode(newVal, true).getStops();
                List<Route> routeResult = GetGeocode.getGeocode(newVal, true).getRoutes();
                Platform.runLater(() -> {
                    resultsBox.getChildren().clear();

                    if (!routeResult.isEmpty()) {
                        Label routeSectionLabel = new Label("Járatok");
                        routeSectionLabel.getStyleClass().add("subheading");
                        resultsBox.getChildren().add(routeSectionLabel);

                        for (Route route : routeResult) {
                            HBox routeItem = new HBox(10);

                            // Járatszám badge
                            Label routeLabel = new Label(route.getRouteShortName());
                            routeLabel.getStyleClass().add("route-badge");
                            routeLabel.setStyle(
                                    "-fx-background-color: " + route.getColor() + ";" +
                                            "-fx-text-fill: " + route.getTextColor() + ";"
                            );

                            // Úticél / headsign
                            Label destinationLabel = new Label("▶ " + route.getHeadsign());
                            destinationLabel.getStyleClass().add("destination");

                            routeItem.getChildren().addAll(routeLabel, destinationLabel);
                            routeItem.getStyleClass().add("transport-modes-grid");
                            routeItem.setAlignment(Pos.CENTER_LEFT);

                            resultsBox.getChildren().add(routeItem);}

                        // Elválasztó vonal
                        Region separator = new Region();
                        separator.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 0 1 0;");
                        separator.setPrefHeight(16);
                        resultsBox.getChildren().add(separator);
                    }

                    for (Stop stop : stopResults) {
                        HBox stopItem = new HBox(10);

                        Label nameLabel = new Label(stop.getName());
                        nameLabel.getStyleClass().add("timelabel");
                        stopItem.getChildren().add(nameLabel);

                        Region wideSeparator = new Region();
                        HBox.setHgrow(wideSeparator, Priority.ALWAYS);
                        stopItem.getChildren().add(wideSeparator);


                        Label postCodeLabel = new Label();
                        postCodeLabel.setText(stop.getPostcode() != null ? "(" + stop.getPostcode() + ")" : "(" + stop.getSubTitle() + ")");
                        postCodeLabel.getStyleClass().add("subheading");
                        stopItem.getChildren().add(postCodeLabel);


                        stopItem.getStyleClass().add("transport-modes-grid");
                        stopItem.getStyleClass().add("clickable");
                        stopItem.setMaxWidth(Double.MAX_VALUE);

                        stopItem.setOnMouseClicked(_ -> SceneManager.show(new StopView(stop, date)));
                        addHoverScaleEffect(stopItem, 1.05);


                        resultsBox.getChildren().add(stopItem);
                    }
                });
            } catch (Exception e) {
                System.out.println("Hiba történt: " + e.getMessage());
            }
        }).start());

        root.getChildren().addAll(searchField, resultsBox);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
