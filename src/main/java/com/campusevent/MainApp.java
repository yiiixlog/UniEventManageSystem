package com.campusevent;

import com.campusevent.ui.AppContext;
import com.campusevent.ui.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        AppContext context = new AppContext(stage);
        Scene scene = new Scene(new LoginView(context), AppContext.WINDOW_WIDTH, AppContext.WINDOW_HEIGHT);
        stage.setTitle("Campus Event Management System");
        stage.setMinWidth(AppContext.WINDOW_WIDTH);
        stage.setMinHeight(AppContext.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
