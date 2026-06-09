package com.campusevent;

import com.campusevent.ui.AppContext;
import com.campusevent.ui.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static final int WINDOW_WIDTH = 1220;
    private static final int WINDOW_HEIGHT = 720;

    @Override
    public void start(Stage stage) {
        AppContext context = new AppContext(stage);
        Scene scene = new Scene(new LoginView(context), WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle("Campus Event Management System");
        stage.setMinWidth(WINDOW_WIDTH);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
