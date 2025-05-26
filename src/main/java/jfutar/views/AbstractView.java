package jfutar.views;

import javafx.scene.Parent;
import jfutar.views.details.*;
import jfutar.views.planner.*;

/**
 * Ez az absztrakt osztály határozza meg a view-k alapfelépítését.
 * Minden konkrét view ezt örökli, és implementálja a {@code start}, {@code stop} és {@code getView} metódusokat.
 */
public sealed abstract class AbstractView permits StopSearchView, SearchView, PlanView, ItinerariesView, TripView, StopView, FavoriteTripsView, FavoriteStopsView, HistoryView {
    /**
     * Esemény, amit akkor hívunk, amikor a view láthatóvá válik.
     * Például: adatok frissítése, automatikus háttérfolyamatok indítása.
     */
    public abstract void start();

    /**
     * Esemény, amit akkor hívunk, amikor a view elhagyásra kerül.
     * Például: háttérszálak leállítása.
     */
    public abstract void stop();

    /**
     * Visszaadja a JavaFX UI struktúrát, amit meg kell jeleníteni.
     *
     * @return egy {@link Parent}, ami a nézet vizuális elemeit tartalmazza
     */
    public abstract Parent getView();
}