module com.lihtar.lihtar {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.media;


    opens com.lihtar.lihtar to javafx.fxml;
    exports com.lihtar.lihtar;

    opens com.lihtar.lihtar.Scenes to javafx.fxml;
    exports com.lihtar.lihtar.Scenes;
}