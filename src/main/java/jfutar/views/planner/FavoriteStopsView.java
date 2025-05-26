package jfutar.views.planner;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import jfutar.managers.FavoritesManager;
import jfutar.managers.SceneManager;
import jfutar.records.stop.Stop;
import jfutar.views.AbstractView;
import jfutar.views.details.StopView;

import java.time.LocalDate;
import java.util.List;

import static jfutar.managers.UtilityManager.*;

/**
 * A kedvenc megállókat jeleníti meg listában.
 * A felhasználó innen navigálhat tovább egy megálló részletes nézetére.
 */
public final class FavoriteStopsView extends AbstractView {

    public FavoriteStopsView() {
        // Nem kell paraméter
    }

    /**
     * Létrehozza a teljes view-t ScrollPane-ben.
     *
     * @return a view UI eleme
     */
    public Parent getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("main-screen");

        HBox titleBox = new HBox(10);
        Button backButton = createBackButton();
        titleBox.getChildren().add(backButton);

        Label title = new Label("Kedvenc megállók");
        title.getStyleClass().add("heading");
        titleBox.getChildren().add(title);
        root.getChildren().add(titleBox);

        root.getChildren().add(new Separator());

        VBox stopsBox = new VBox(8);
        stopsBox.getStyleClass().add("transport-modes-grid");

        List<Stop> favoriteStops = FavoritesManager.getFavoriteStops();

        if (favoriteStops.isEmpty()) {
            Label emptyLabel = new Label("Nincsenek kedvenc megállók.");
            stopsBox.getChildren().add(emptyLabel);
        } else {
            for (int i = 0; i < favoriteStops.size(); i++) {
                Stop stop = favoriteStops.get(i);

                HBox stopBox = new HBox();
                stopBox.getStyleClass().add("route-step");
                stopBox.setAlignment(Pos.CENTER_LEFT);
                stopBox.setSpacing(8);

                Button starButton = new Button();
                starButton.getStyleClass().add("favorites-button");
                starButton.getStyleClass().add("clickable");
                addHoverScaleEffect(starButton, 1.15);

                // betöltésnél az e
                if (isFavoriteStop.test(stop)) starButton.getStyleClass().add("favorites-button-active");

                // Kedvencekhez adás
                starButton.setOnAction(_ -> toggleFavoriteStop(stop, starButton));

                Label nameLabel = new Label(stop.getName());
                nameLabel.getStyleClass().add("timelabel");
                nameLabel.getStyleClass().add("clickable");


                nameLabel.setOnMouseClicked(_ -> SceneManager.show(new StopView(stop, LocalDate.now().toString().replace("-", ""))));
                /* Rendesen kiírt Consumerrel is megoldható lenne de full fölösleges mert konkrétan egyszer használom ezt
                Consumer<Stop> showStopDetails = s ->
                        SceneManager.show(new StopView(s, LocalDate.now().toString().replace("-", "")));
                nameLabel.setOnMouseClicked(_ -> showStopDetails.accept(stop));
                */

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label idLabel = new Label(stop.getId());
                idLabel.getStyleClass().add("subheading");

                stopBox.getChildren().addAll(starButton, nameLabel, spacer, idLabel);
                stopsBox.getChildren().add(stopBox);

                if (i < favoriteStops.size() - 1)
                    stopsBox.getChildren().add(new Separator());
            }
        }
        root.getChildren().add(stopsBox);

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
