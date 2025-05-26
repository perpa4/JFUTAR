package jfutar.views.planner;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import jfutar.managers.SceneManager;
import jfutar.records.stop.Stop;
import jfutar.requests.GetGeocode;
import jfutar.views.AbstractView;

import java.util.List;
import java.util.function.Consumer;

import static jfutar.managers.UtilityManager.addHoverScaleEffect;
import static jfutar.managers.UtilityManager.createBackButton;

/**
 * Ez a view egy Stop kiválasztására szolgál keresés alapján.
 * A kiválasztott Stop visszaadásra kerül a hívó view számára a callback-kel.
 */
public final class StopSearchView extends AbstractView {
    // Ezzel birom visszaadni az értéket az előző Viewnek
    // Callback típus amit átadok az adott viewnek
    // Kiválasztok egy Stopot akkor meghívódik és átadom neki a Stopot

    // Egyénként elég hasznos, pl forEachnél stream apinál kiíratni dolgokat lambda expressionnel
    // .stream().forEach(num, -> System.out.println(num))

    // Indoklás: Nem nagyon szeretném itt létrehozni a planView-et úgy, hogy a konstruktornak átadom a kiválasztott megállót innen, mert az hülyeség lenne, és teljesen felborítaná a SceneManagerem működését
    // Átadni könnyű lenne következő Viewnek (Szóval nem kellene Supplier<T>), amit a mostani Viewből nyitok meg, hiszen a Stack miatt teljesen doable, de fordítva nem.
    // Nem magamtól jöttem rá erre nyilván, hiszen nem tanultuk, de meg tanultam használni az összes FunctionalInterface-t.
    private final Consumer<Stop> onStopSelected;

    /**
     * Konstruktor, amelyben megadjuk, hogyan térjen vissza az eredmény (Stop).
     *
     * @param onStopSelected a kiválasztott Stop-ot fogadó callback
     */
    public StopSearchView(Consumer<Stop> onStopSelected) {
        this.onStopSelected = onStopSelected;
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
        searchField.setPromptText("Kezdj el írni egy megállónevet...");
        searchField.getStyleClass().add("search-field");

        VBox resultsBox = new VBox(8);

        searchField.textProperty().addListener((_, _, newVal) -> new Thread(() -> {
            try {
                List<Stop> results = GetGeocode.getGeocode(newVal, false).getStops();
                Platform.runLater(() -> {
                    resultsBox.getChildren().clear();

                    // Itt pl, át tudom alakítani streamesre a foreach-t, consumer-t meg minden jót lehet használni
                    results.forEach(stop -> {
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
                        stopItem.setOnMouseClicked(_ -> {
                            // Itt van callback
                            onStopSelected.accept(stop);
                            SceneManager.back();
                        });
                        addHoverScaleEffect(stopItem, 1.05);
                        resultsBox.getChildren().add(stopItem);
                    });
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
