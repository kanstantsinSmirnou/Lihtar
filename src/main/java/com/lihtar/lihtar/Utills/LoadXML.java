package com.lihtar.lihtar.Utills;

import javafx.fxml.FXMLLoader;
import java.io.IOException;
import com.lihtar.lihtar.StartApplication;

public class LoadXML {
     public static FXMLLoader load(String path) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(StartApplication.class.getResource(path));
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return loader;
     }
}
