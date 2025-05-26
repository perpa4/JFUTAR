package jfutar.views.planner;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jfutar.managers.FavoritesManager;
import jfutar.managers.SceneManager;
import jfutar.records.trip.SaveableTrip;
import jfutar.views.AbstractView;

import java.util.List;

import static jfutar.managers.UtilityManager.*;

/**
 * A kedvenc útvonalakat jeleníti meg listában.
 * A felhasználó innen navigálhat tovább egy megálló részletes nézetére.
 */
public final class FavoriteTripsView extends AbstractView {

    private final String date;
    private final String time;
    private final String modes;
    private final boolean arriveBy;
    private final boolean wheelchair;

    public FavoriteTripsView(String date, String time, String modes, boolean arriveBy, boolean wheelchair) {
        this.date = date;
        this.time = time;
        this.modes = modes;
        this.arriveBy = arriveBy;
        this.wheelchair = wheelchair;
    }

    /**
     * Létrehozza a teljes view-t ScrollPane-ben.
     *
     * @return a view UI eleme
     */
    public ScrollPane getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("main-screen");

        HBox titleBox = new HBox(10);
        Button backButton = createBackButton();
        titleBox.getChildren().add(backButton);

        Label title = new Label("Kedvenc útvonalak");
        title.getStyleClass().add("heading");
        titleBox.getChildren().add(title);
        root.getChildren().add(titleBox);

        root.getChildren().add(new Separator());

        VBox stopsBox = new VBox(8);
        stopsBox.getStyleClass().add("transport-modes-grid");

        List<SaveableTrip> favoriteTrips = FavoritesManager.getFavoriteTrips();

        if (favoriteTrips.isEmpty()) {
            Label emptyLabel = new Label("Nincsenek kedvenc útvonalak.");
            stopsBox.getChildren().add(emptyLabel);
        } else {
            for (int i = 0; i < favoriteTrips.size(); i++) {
                SaveableTrip trip = favoriteTrips.get(i);

                HBox stopBox = new HBox();
                stopBox.getStyleClass().add("route-step");
                stopBox.setAlignment(Pos.CENTER_LEFT);
                stopBox.setSpacing(8);

                Button starButton = new Button();
                starButton.getStyleClass().add("favorites-button");
                starButton.getStyleClass().add("clickable");
                addHoverScaleEffect(starButton, 1.15);

                // betöltésnél az e
                if (isFavoriteTrip.test(trip)) starButton.getStyleClass().add("favorites-button-active");

                // Kedvencekhez adás
                starButton.setOnAction(_ -> toggleFavoriteTrip(trip, starButton));

                Label nameLabel = new Label(trip.getFrom().getName() + " - " + trip.getTo().getName());
                nameLabel.getStyleClass().add("timelabel");
                nameLabel.getStyleClass().add("clickable");
                nameLabel.setOnMouseClicked(_ -> SceneManager.show(new ItinerariesView(
                        trip.getFrom(),
                        trip.getTo(),
                        date.replace("-",""),
                        time,
                        modes,
                        arriveBy,
                        wheelchair
                )));

                stopBox.getChildren().addAll(starButton, nameLabel);
                stopsBox.getChildren().add(stopBox);

                if (i < favoriteTrips.size() - 1)
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
