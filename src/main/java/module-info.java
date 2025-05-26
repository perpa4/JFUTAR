module hu.itk.ppke.jfutar {
    requires javafx.fxml;
    requires javafx.controls;
    requires com.google.gson;
    requires java.net.http;
    requires org.yaml.snakeyaml;
    requires java.desktop;

    // Hozzá kellett adni, hogy megfelelően tudjam menteni a kedvenceket
    exports jfutar.managers;
    opens jfutar.managers to org.yaml.snakeyaml;

    exports jfutar.records.stop;
    exports jfutar.records.itinerary;
    exports jfutar.records.trip;
    exports jfutar.records.search;

    exports jfutar.views;
    exports jfutar.views.planner;
    exports jfutar.requests;

    opens jfutar.requests to javafx.fxml;
    opens jfutar.views to javafx.fxml;
}