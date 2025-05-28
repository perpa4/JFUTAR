package jfutar.views;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import jfutar.managers.FavoritesManager;
import jfutar.managers.HistoryManager;
import jfutar.managers.SceneManager;
import jfutar.views.planner.PlanView;

import java.util.Objects;


/**
 * [CREDITS]
 * <a href="https://dribbble.com/shots/12324722-Bus-booking-app">Design Ötletek</a>
 */
public final class Main extends Application {

    // Igen, benne hagytam az API kulcsot, de törölve van lol, lényegtelen.
    public static final String apiKey = "fc11cf25-f339-40a3-ad5d-d76f8898aa86";
    public static final PlanView PLAN_VIEW = new PlanView();

    @Override
    public void start(Stage stage) {
        // Ettől elvileg szépek lesznek a fontok mert amúgy okádék hogy nincs anti-aliasing
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");

        FavoritesManager.loadFavorites();
        HistoryManager.loadHistory();

        SceneManager.setStage(stage);
        SceneManager.show(PLAN_VIEW);

        stage.setTitle("JFUTAR Kliens");
        stage.getIcons().add(
                new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/logo.png")))
        );
        stage.setResizable(true);
        stage.show();
        stage.setMinHeight(860);
        stage.setMinWidth(470);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
