package com.campusevent.ui;

import com.campusevent.model.Organizer;
import com.campusevent.model.Student;
import com.campusevent.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class MainView extends BorderPane {

    public MainView(AppContext context) {
        setStyle("-fx-background-color: #ffffff;");

        User user = context.getCurrentUser();
        Label title = new Label("Campus Event Management System");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2f3437;");

        Label userLabel = new Label(user.getName() + " / " + user.getRole());
        userLabel.setStyle("-fx-text-fill: #526b68;");

        Button logoutButton = new Button("登出");
        logoutButton.setOnAction(event -> {
            context.setCurrentUser(null);
            context.navigateTo(new LoginView(context));
        });

        HBox header = new HBox(16, title, userLabel, logoutButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 24, 18, 24));
        header.setStyle("-fx-background-color: #d8e2dc;");
        setTop(header);

        if (user instanceof Student) {
            setCenter(new StudentView(context, (Student) user));
        } else if (user instanceof Organizer) {
            setCenter(new OrganizerView(context, (Organizer) user));
        } else {
            setCenter(new Label("Unknown role: " + user.getRole()));
        }
    }
}
