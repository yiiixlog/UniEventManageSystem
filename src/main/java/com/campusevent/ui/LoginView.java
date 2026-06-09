package com.campusevent.ui;

import com.campusevent.model.User;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class LoginView extends VBox {
    private static final int RECENT_ACCOUNT_LIMIT = 5;
    private static final String RECENT_ACCOUNT_KEY = "recentAccounts";
    private static final String ACCOUNT_SEPARATOR = ",";
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(LoginView.class);

    public LoginView(AppContext context) {
        setAlignment(Pos.CENTER);
        setSpacing(16);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: #f7f8f5;");

        Label title = new Label("Campus Event Management System");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2f3437;");

        Label subtitle = new Label("校園活動管理系統");
        subtitle.setStyle("-fx-font-size: 18px; -fx-text-fill: #6f8f8b;");

        ComboBox<String> accountBox = new ComboBox<>();
        accountBox.setEditable(true);
        accountBox.setPromptText("學號 / 教師編號");
        accountBox.setItems(FXCollections.observableArrayList(loadRecentAccounts()));
        accountBox.setVisibleRowCount(RECENT_ACCOUNT_LIMIT);
        accountBox.setMaxWidth(320);
        configureRecentAccountDropdown(accountBox);
        accountBox.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (isFocused && !accountBox.getItems().isEmpty()) {
                accountBox.show();
            }
        });
        accountBox.getEditor().focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (isFocused && !accountBox.getItems().isEmpty()) {
                accountBox.show();
            }
        });
        accountBox.setOnMouseClicked(event -> {
            if (!accountBox.getItems().isEmpty()) {
                accountBox.show();
            }
        });

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("密碼");
        passwordField.setMaxWidth(320);

        Button loginButton = new Button("登入");
        loginButton.setDefaultButton(true);
        loginButton.setStyle("-fx-background-color: #6f8f8b; -fx-text-fill: white; -fx-font-weight: bold;");

        loginButton.setOnAction(event -> {
            String account = accountBox.getEditor().getText().trim();
            Optional<User> user = context.getAuthService().login(account, passwordField.getText());
            if (user.isPresent()) {
                saveRecentAccount(account);
                context.setCurrentUser(user.get());
                context.navigateTo(new MainView(context));
            } else {
                showMessage(Alert.AlertType.ERROR, "登入失敗", "請確認帳號與密碼是否正確。");
            }
        });

        TextField demoAccounts = new TextField("Demo: A11423011 / 1234 或 001 / 1234");
        demoAccounts.setEditable(false);
        demoAccounts.setMaxWidth(360);
        demoAccounts.setStyle("-fx-text-fill: #70777a; -fx-background-color: transparent; "
                + "-fx-border-color: transparent; -fx-padding: 4 0 4 0;");

        getChildren().addAll(title, subtitle, accountBox, passwordField, loginButton, demoAccounts);
    }

    private void configureRecentAccountDropdown(ComboBox<String> accountBox) {
        accountBox.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String account, boolean empty) {
                super.updateItem(account, empty);
                if (empty || account == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label accountLabel = new Label(account);
                accountLabel.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(accountLabel, Priority.ALWAYS);

                Button deleteButton = new Button("x");
                deleteButton.setFocusTraversable(false);
                deleteButton.setStyle("-fx-padding: 1 6 1 6; -fx-font-size: 11px; "
                        + "-fx-background-radius: 10; -fx-text-fill: #70777a;");
                deleteButton.setOnAction(event -> {
                    event.consume();
                    accountBox.getItems().remove(account);
                    removeRecentAccount(account);
                    if (accountBox.getItems().isEmpty()) {
                        accountBox.hide();
                    } else {
                        accountBox.show();
                    }
                });

                HBox row = new HBox(8, accountLabel, deleteButton);
                row.setAlignment(Pos.CENTER_LEFT);
                setText(null);
                setGraphic(row);
            }
        });
    }

    private List<String> loadRecentAccounts() {
        String savedAccounts = PREFERENCES.get(RECENT_ACCOUNT_KEY, "");
        if (savedAccounts.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(savedAccounts.split(ACCOUNT_SEPARATOR))
                .map(String::trim)
                .filter(account -> !account.isEmpty())
                .distinct()
                .limit(RECENT_ACCOUNT_LIMIT)
                .collect(Collectors.toList());
    }

    private void saveRecentAccount(String account) {
        if (account.isEmpty()) {
            return;
        }

        List<String> accounts = new ArrayList<>();
        accounts.add(account);
        loadRecentAccounts().stream()
                .filter(savedAccount -> !savedAccount.equals(account))
                .forEach(accounts::add);

        String savedAccounts = accounts.stream()
                .limit(RECENT_ACCOUNT_LIMIT)
                .collect(Collectors.joining(ACCOUNT_SEPARATOR));
        PREFERENCES.put(RECENT_ACCOUNT_KEY, savedAccounts);
    }

    private void removeRecentAccount(String account) {
        String savedAccounts = loadRecentAccounts().stream()
                .filter(savedAccount -> !savedAccount.equals(account))
                .collect(Collectors.joining(ACCOUNT_SEPARATOR));
        PREFERENCES.put(RECENT_ACCOUNT_KEY, savedAccounts);
    }

    private void showMessage(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
