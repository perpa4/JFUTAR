package jfutar.managers;

import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import jfutar.views.AbstractView;

import java.util.Objects;
import java.util.Stack;

import static jfutar.views.Main.PLAN_VIEW;

/**
 * Ez az osztály felelős a Scene/Panek kezeléséért.
 * Kellemes mellékhatása a teljes osztálypéldány átadásának az autómatikus ujratöltés Scene váltás esetén.
 */
public final class SceneManager {
    private static AbstractView currentView;
    private static final Stack<AbstractView> viewStack = new Stack<>();
    private static StackPane rootLayout;

    /**
     * Inicializálja az alkalmazás főablakát (stage), beállítja az alappanelt,
     * CSS-t, valamint a bezárás eseményt (leállítja az aktuális view-t).
     *
     * @param stage a JavaFX Stage példány
     */
    public static void setStage(Stage stage) {
        rootLayout = new StackPane();
        Scene scene = new Scene(rootLayout, 450, 800);
        scene.getStylesheets().add(Objects.requireNonNull(SceneManager.class.getResource("/styles.css")).toExternalForm());
        stage.setScene(scene);

        // App bezárásakor leállítjuk a jelenlegi view-t (és ezzel a threadjeit is)
        stage.setOnCloseRequest(_ -> {
            if (currentView != null) currentView.stop();
            System.out.println("Alkalmazás bezárva, háttérszálak leállítva.");
        });
    }

    /**
     * Megjelenít egy új view-t animációval, leállítva az előzőt.
     * A fejemben működött de itt nem akar teljes mértékben.
     * @param newView az új megjelenítendő {@link AbstractView}
     */
    public static void show(AbstractView newView) {
        // Ha van jelenlegi view, leállítjuk azt, és elmentjük a stack-be (visszalépéshez)
        if (currentView != null) {
            currentView.stop();
            viewStack.push(currentView);
        }

        // A jelenlegi view-t elmentjük previousként, hogy tudjuk animálni
        AbstractView previousView = currentView;

        // Uj view-t beállítjuk azt elindítjuk
        currentView = newView;
        currentView.start();

        // Új view view-je 💀
        Node newNode = newView.getView();

        // Ha volt előző view, akkor annak is
        Node oldNode = previousView != null ? previousView.getView() : null;

        // Az új view a képernyő jobb oldalán kívülről indul
        newNode.setTranslateX(rootLayout.getWidth()); // új view jobbról indul

        // Hozzáadjuk a view-t a StackPane-hez (a régivel együtt lesz benne)
        rootLayout.getChildren().add(newNode);

        // Animáció - az új view 250ms alatt becsúszik jobbról középre
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), newNode);
        slideIn.setToX(0);

        // Ha van régi view, akkor azt balra ki kell animálni (mindig lesz, hacsak a semmit töltjük be)
        if (oldNode != null) {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), oldNode);
            slideOut.setToX(-rootLayout.getWidth());

            // Amint az animáció véget ér, eltávolítjuk a régi view-t
            slideOut.setOnFinished(_ -> rootLayout.getChildren().remove(oldNode));
            slideOut.play();
        }

        slideIn.play();
    }

    /**
     * Visszalép az előző view-ra animációval, vagy ha már nincs más, visszatér a fő PlanView-hoz.
     * A fejemben működött de itt nem akar teljes mértékben.
     */
    public static void back() {
        // Ha nincs korábbi view, nincs mit csinálni
        if (viewStack.isEmpty()) return;

        // Leállítjuk az aktuális view száljait
        currentView.stop();
        AbstractView oldView = currentView;
        AbstractView newView;

        // Ha már csak egy van a stackben, visszatérünk a planView-hez
        if (viewStack.size() < 2) {
            newView = PLAN_VIEW;
            viewStack.clear();
        }
        else newView = viewStack.pop();

        // Beállítjuk az új aktuális view-t és elindítjuk
        currentView = newView;
        currentView.start();

        // Lekérjük a view-ek viewjeit lol
        Node newNode = newView.getView();
        Node oldNode = oldView.getView();

        newNode.setTranslateX(-rootLayout.getWidth()); // új view balról jön be
        rootLayout.getChildren().add(newNode);

        // Az új view 250ms alatt balról becsúszik
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), newNode);
        slideIn.setToX(0);

        // A régi view 250ms alatt jobbra kicsúszik
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), oldNode);
        slideOut.setToX(rootLayout.getWidth());

        // Az animáció végén eltávolítjuk a régi view-t a StackPane-ből
        slideOut.setOnFinished(_ -> rootLayout.getChildren().remove(oldNode));

        // Egyszerre
        slideOut.play();
        slideIn.play();
    }
}