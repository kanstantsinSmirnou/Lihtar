module com.lihtar.lihtar {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.lihtar.lihtar to javafx.fxml;
    exports com.lihtar.lihtar;
}