package com.lihtar.lihtar;

import com.lihtar.lihtar.Data.User;
import com.lihtar.lihtar.Data.Database;
import com.lihtar.lihtar.Utills.LoadXML;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class StartApplicationController extends StartApplication {
    @FXML
    private TextField userLogin;
    @FXML
    private Label incorrectUserDataLabel;
    @FXML
    private TextField userPassword;
    public void initialize() {
    }
    @FXML
    protected void logInButton() {
        try {
            User.MainUser = Database.getUser(userLogin.getText(), userPassword.getText());
            FXMLLoader loader = LoadXML.load("Scenes/MainPageView.fxml");
            setScene(loader);
        } catch (Database.IncorrectUserDataException e) {
            incorrectUserDataLabel.setText("Incorrect username or password");
            incorrectUserDataLabel.setTextFill(Color.web("#dd0e0e", 0.8));
        } catch (Database.InvalidCharactersException e) {
            incorrectUserDataLabel.setText("You use symbols that cannot be used");
            incorrectUserDataLabel.setTextFill(Color.web("#dd0e0e", 0.8));
        } catch (Database.EmptyStringException e) {
            incorrectUserDataLabel.setText("Fields cannot be empty");
            incorrectUserDataLabel.setTextFill(Color.web("#dd0e0e", 0.8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void signUpButton() {
        FXMLLoader loader = LoadXML.load("Scenes/SignUpView.fxml");
        setScene(loader);
    }
}