package com.campusevent.ui;

import com.campusevent.model.Event;
import com.campusevent.model.Registration;
import com.campusevent.model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StudentView extends BorderPane {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String ALL_TYPES = "全部類型";

    private final AppContext context;
    private final Student student;
    private final ObservableList<Event> events = FXCollections.observableArrayList();
    private final ObservableList<Registration> registrationRecords = FXCollections.observableArrayList();
    private final ListView<Event> eventList = new ListView<>(events);
    private final ListView<Registration> registrationList = new ListView<>(registrationRecords);
    private final TextField searchField = new TextField();
    private final DatePicker datePicker = new DatePicker();
    private final ComboBox<String> typeFilter = new ComboBox<>();
    private final Label detailLabel = new Label("請選擇活動");
    private final Label eventSummaryLabel = new Label();

    public StudentView(AppContext context, Student student) {
        this.context = context;
        this.student = student;

        setPadding(new Insets(24));
        setStyle("-fx-background-color: #ffffff;");

        configureEventList();
        configureRegistrationList();

        VBox leftPane = new VBox(12, createToolbar(), eventSummaryLabel, eventList);
        VBox.setVgrow(eventList, Priority.ALWAYS);

        Button registerButton = new Button("報名活動");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setOnAction(event -> registerSelectedEvent());

        Button cancelRegistrationButton = new Button("取消選取的報名");
        cancelRegistrationButton.setMaxWidth(Double.MAX_VALUE);
        cancelRegistrationButton.setOnAction(event -> cancelSelectedRegistration());

        Label registrationTitle = new Label("我的報名紀錄");
        registrationTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2f3437;");

        detailLabel.setWrapText(true);
        detailLabel.setStyle("-fx-font-size: 14px; -fx-line-spacing: 4px;");

        VBox rightPane = new VBox(14, detailLabel, registerButton, registrationTitle, registrationList, cancelRegistrationButton);
        rightPane.setPadding(new Insets(0, 0, 0, 24));
        rightPane.setPrefWidth(390);
        VBox.setVgrow(registrationList, Priority.ALWAYS);

        setCenter(leftPane);
        setRight(rightPane);

        loadEventTypes();
        applyFilters();
        refreshRegistrations();
    }

    private HBox createToolbar() {
        searchField.setPromptText("搜尋活動名稱、地點、類型...");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        typeFilter.setPrefWidth(130);
        datePicker.setPromptText("日期");

        Button searchButton = new Button("查詢");
        searchButton.setOnAction(event -> applyFilters());

        Button resetButton = new Button("清除");
        resetButton.setOnAction(event -> {
            searchField.clear();
            datePicker.setValue(null);
            typeFilter.getSelectionModel().select(ALL_TYPES);
            applyFilters();
        });

        HBox toolbar = new HBox(10, searchField, datePicker, typeFilter, searchButton, resetButton);
        toolbar.setPadding(new Insets(0, 0, 8, 0));
        return toolbar;
    }

    private void configureEventList() {
        eventList.setPlaceholder(new Label("找不到符合條件的活動"));
        eventList.setCellFactory(listView -> new ListCell<Event>() {
            @Override
            protected void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);
                if (empty || event == null) {
                    setText(null);
                    return;
                }

                int registeredCount = context.getRegistrationService().countActiveRegistrations(event.getEventId());
                setText(event.getTitle()
                        + "\n" + event.getStartTime().format(DISPLAY_TIME)
                        + " / " + event.getLocation()
                        + "\n" + event.getEventType()
                        + " / " + event.getStatus(registeredCount)
                        + " / " + registeredCount + " / " + event.getCapacity());
            }
        });

        eventList.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> showEventDetail(newValue));
    }

    private void configureRegistrationList() {
        registrationList.setPlaceholder(new Label("目前沒有報名紀錄"));
        registrationList.setCellFactory(listView -> new ListCell<Registration>() {
            @Override
            protected void updateItem(Registration registration, boolean empty) {
                super.updateItem(registration, empty);
                if (empty || registration == null) {
                    setText(null);
                    return;
                }

                setText(formatRegistration(registration));
            }
        });
    }

    private void loadEventTypes() {
        List<String> types = new ArrayList<>();
        types.add(ALL_TYPES);
        types.addAll(context.getEventService().getEventTypes());
        typeFilter.setItems(FXCollections.observableArrayList(types));
        typeFilter.getSelectionModel().select(ALL_TYPES);
    }

    private void applyFilters() {
        LocalDate selectedDate = datePicker.getValue();
        String selectedType = typeFilter.getSelectionModel().getSelectedItem();
        if (ALL_TYPES.equals(selectedType)) {
            selectedType = "";
        }

        events.setAll(context.getEventService().findEvents(searchField.getText(), selectedDate, selectedType));
        eventSummaryLabel.setText("共 " + events.size() + " 筆活動，依活動時間排序");

        if (!events.isEmpty()) {
            eventList.getSelectionModel().selectFirst();
        } else {
            showEventDetail(null);
        }
    }

    private void showEventDetail(Event event) {
        if (event == null) {
            detailLabel.setText("請選擇活動");
            return;
        }

        int registeredCount = context.getRegistrationService().countActiveRegistrations(event.getEventId());
        detailLabel.setText(
                "活動：" + event.getTitle()
                        + "\n類型：" + event.getEventType()
                        + "\n地點：" + event.getLocation()
                        + "\n主辦單位：" + event.getOrganizerName()
                        + "\n時間：" + event.getStartTime().format(DISPLAY_TIME)
                        + " - " + event.getEndTime().format(DISPLAY_TIME)
                        + "\n名額：" + registeredCount + " / " + event.getCapacity()
                        + "\n狀態：" + event.getStatus(registeredCount)
                        + "\n\n" + event.getDescription()
        );
    }

    private void registerSelectedEvent() {
        Event event = eventList.getSelectionModel().getSelectedItem();
        if (event == null) {
            showMessage(Alert.AlertType.INFORMATION, "請先選擇活動");
            return;
        }

        try {
            context.getRegistrationService().register(student, event);
            showMessage(Alert.AlertType.INFORMATION, "報名成功");
            refreshAfterRegistrationChange(event);
        } catch (IllegalArgumentException exception) {
            showMessage(Alert.AlertType.WARNING, exception.getMessage());
        }
    }

    private void refreshRegistrations() {
        registrationRecords.setAll(context.getRegistrationService().getRegistrationsByStudent(student.getStudentNo()));
    }

    private void cancelSelectedRegistration() {
        Registration registration = registrationList.getSelectionModel().getSelectedItem();
        if (registration == null) {
            showMessage(Alert.AlertType.INFORMATION, "請先選擇一筆報名紀錄");
            return;
        }

        if (!registration.isActive()) {
            showMessage(Alert.AlertType.INFORMATION, "這筆報名已取消，無需重複取消");
            return;
        }

        context.getRegistrationService().cancelRegistration(registration.getRegistrationId());
        showMessage(Alert.AlertType.INFORMATION, "已取消報名");

        Event selectedEvent = eventList.getSelectionModel().getSelectedItem();
        refreshAfterRegistrationChange(selectedEvent);
    }

    private void refreshAfterRegistrationChange(Event selectedEvent) {
        applyFilters();
        if (selectedEvent != null) {
            events.stream()
                    .filter(event -> event.getEventId() == selectedEvent.getEventId())
                    .findFirst()
                    .ifPresent(event -> eventList.getSelectionModel().select(event));
        }
        refreshRegistrations();
        showEventDetail(eventList.getSelectionModel().getSelectedItem());
    }

    private String formatRegistration(Registration registration) {
        String eventTitle = context.getEventService()
                .findById(registration.getEventId())
                .map(Event::getTitle)
                .orElse("活動已刪除");

        return eventTitle
                + "\n狀態：" + displayStatus(registration.getStatus())
                + " / 報名時間：" + formatDateTime(registration.getRegisteredAt());
    }

    private String displayStatus(String status) {
        if ("REGISTERED".equals(status)) {
            return "已報名";
        }
        if ("CANCELLED".equals(status)) {
            return "已取消";
        }
        return status;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DISPLAY_TIME);
    }

    private void showMessage(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("系統提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
