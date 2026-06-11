package com.campusevent.ui;

import com.campusevent.model.User;
import com.campusevent.service.AuthService;
import com.campusevent.service.CsvExporter;
import com.campusevent.service.EventService;
import com.campusevent.service.RegistrationService;
import com.campusevent.storage.FileStorage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppContext {
    public static final int WINDOW_WIDTH = 1220;
    public static final int WINDOW_HEIGHT = 820;

    private final Stage stage;
    private final FileStorage fileStorage;
    private final AuthService authService;
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final CsvExporter csvExporter;

    private User currentUser;

    public AppContext(Stage stage) {
        this.stage = stage;
        this.fileStorage = new FileStorage();
        this.authService = new AuthService(fileStorage);
        this.eventService = new EventService(fileStorage);
        this.registrationService = new RegistrationService(fileStorage);
        this.csvExporter = new CsvExporter(fileStorage, registrationService);
    }

    public void navigateTo(Parent parent) {
        Scene scene = stage.getScene();
        if (scene == null) {
            stage.setScene(new Scene(parent, WINDOW_WIDTH, WINDOW_HEIGHT));
        } else {
            scene.setRoot(parent);
        }
    }

    public Stage getStage() {
        return stage;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public EventService getEventService() {
        return eventService;
    }

    public RegistrationService getRegistrationService() {
        return registrationService;
    }

    public CsvExporter getCsvExporter() {
        return csvExporter;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
