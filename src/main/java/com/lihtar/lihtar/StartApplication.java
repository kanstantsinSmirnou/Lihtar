package com.lihtar.lihtar;

import com.lihtar.lihtar.Data.SQLBase.SqlCommunicate;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import com.lihtar.lihtar.Data.SQLBase.SqlCommunicate;

public class StartApplication extends Application {

    public static Stage primaryStage;
    public static void setScene(FXMLLoader loader) {
        Parent root = loader.getRoot();
        ((Stage) primaryStage.getScene().getWindow()).setScene(new Scene(root));
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
//        FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("StartApplicationView.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("StartApplicationView.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Lihtar");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        try {
            SqlCommunicate.connect("jdbc:postgresql://localhost:5432/postgres", "postgres", "1234");
        } catch(Exception e) {
            e.printStackTrace();
        }
        launch();

        SqlCommunicate.disconnect();
    }
}