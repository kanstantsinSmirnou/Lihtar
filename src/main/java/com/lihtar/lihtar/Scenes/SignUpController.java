package com.lihtar.lihtar.Scenes;

import com.lihtar.lihtar.Data.Database;
import com.lihtar.lihtar.Data.User;
import com.lihtar.lihtar.Utills.LoadXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.util.Objects;

import static com.lihtar.lihtar.Data.Database.registerUser;
import static com.lihtar.lihtar.StartApplication.setScene;

public class SignUpController {
    @FXML
    private Label errorLabel;
    @FXML
    private TextField userLogin;
    @FXML
    private TextField userPassword;
    @FXML
    private TextField userPassword2;
    private void setError(String colorString, String errorText) {
        errorLabel.setTextFill(Color.web(colorString, 0.8));
        errorLabel.setText(errorText);
    }
    @FXML
    void signUpButton() {
        try {
            if (!Objects.equals(userPassword.getText(), userPassword2.getText())) {
                setError("#dd0e0e", "Passwords are not equal");
                return;
            }
            registerUser(userLogin.getText(), userPassword.getText());
            setError("#1EA624", "User has been registered");
        } catch (Database.InvalidCharactersException e) {
            setError("#dd0e0e", "You use symbols that cannot be used");
        } catch (Database.EmptyStringException e) {
            setError("#dd0e0e", "Fields cannot be empty");
        } catch(Database.UserAlreadyRegisteredException e) {
            setError("#dd0e0e", "This login is already exists");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void backButton() {
        FXMLLoader loader = LoadXML.load("StartApplicationView.fxml");
        setScene(loader);
    }
}
