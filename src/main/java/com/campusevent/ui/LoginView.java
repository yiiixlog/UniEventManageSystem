package com.campusevent.ui;

import com.campusevent.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class LoginView extends VBox {

    public LoginView(AppContext context) {
        setAlignment(Pos.CENTER);
        setSpacing(16);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: #f7f8f5;");

        Label title = new Label("Campus Event Management System");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2f3437;");

        Label subtitle = new Label("校園活動管理系統");
        subtitle.setStyle("-fx-font-size: 18px; -fx-text-fill: #6f8f8b;");

        TextField accountField = new TextField();
        accountField.setPromptText("學號 / 員工編號");
        accountField.setMaxWidth(320);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("密碼");
        passwordField.setMaxWidth(320);

        Button loginButton = new Button("登入");
        loginButton.setDefaultButton(true);
        loginButton.setStyle("-fx-background-color: #6f8f8b; -fx-text-fill: white; -fx-font-weight: bold;");

        loginButton.setOnAction(event -> {
            Optional<User> user = context.getAuthService().login(accountField.getText(), passwordField.getText());
            if (user.isPresent()) {
                context.setCurrentUser(user.get());
                context.navigateTo(new MainView(context));
            } else {
                showMessage(Alert.AlertType.ERROR, "登入失敗", "請確認帳號與密碼是否正確。");
            }
        });

        Label demoAccounts = new Label("Demo: A11423011 / 1234 或 O001 / 1234");
        demoAccounts.setStyle("-fx-text-fill: #70777a;");

        getChildren().addAll(title, subtitle, accountField, passwordField, loginButton, demoAccounts);
    }

    private void showMessage(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
