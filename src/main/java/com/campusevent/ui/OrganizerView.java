package com.campusevent.ui;

import com.campusevent.model.Event;
import com.campusevent.model.Organizer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrganizerView extends BorderPane {
    private static final DateTimeFormatter INPUT_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AppContext context;
    private final Organizer organizer;
    private final ObservableList<Event> events = FXCollections.observableArrayList();
    private final ListView<Event> eventList = new ListView<>(events);
    private final Label selectedEventLabel = new Label("請選擇活動，或直接填表建立新活動");

    private final TextField titleField = new TextField();
    private final TextField locationField = new TextField();
    private final TextField eventTypeField = new TextField();
    private final TextField startTimeField = new TextField();
    private final TextField endTimeField = new TextField();
    private final TextField capacityField = new TextField();
    private final TextArea descriptionField = new TextArea();

    private Event selectedEvent;

    public OrganizerView(AppContext context, Organizer organizer) {
        this.context = context;
        this.organizer = organizer;

        setPadding(new Insets(24));
        setStyle("-fx-background-color: #ffffff;");

        configureEventList();

        Label title = new Label("主辦者活動管理");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2f3437;");

        Button newButton = new Button("新增活動");
        newButton.setOnAction(event -> {
            selectedEvent = null;
            eventList.getSelectionModel().clearSelection();
            clearFormFields();
            selectedEventLabel.setText("建立新活動");
        });

        Button saveButton = new Button("儲存活動");
        saveButton.setOnAction(event -> saveEvent());

        Button deleteButton = new Button("刪除活動");
        deleteButton.setOnAction(event -> deleteSelectedEvent());

        Button exportButton = new Button("匯出報名名單 CSV");
        exportButton.setOnAction(event -> exportSelectedEvent());

        HBox buttons = new HBox(10, newButton, saveButton, deleteButton, exportButton);

        VBox leftPane = new VBox(12, title, eventList, buttons);
        leftPane.setPrefWidth(450);
        VBox.setVgrow(eventList, Priority.ALWAYS);

        VBox formPane = new VBox(14, selectedEventLabel, new Separator(), createForm());
        formPane.setPadding(new Insets(0, 0, 0, 24));

        setLeft(leftPane);
        setCenter(formPane);

        refreshEvents();
    }

    private void configureEventList() {
        eventList.setPlaceholder(new Label("目前沒有活動"));
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
                        + "\n" + event.getStatus(registeredCount)
                        + " / 報名 " + registeredCount + " / " + event.getCapacity());
            }
        });

        eventList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedEvent = newValue;
            fillForm(newValue);
        });
    }

    private GridPane createForm() {
        titleField.setPromptText("活動標題");
        locationField.setPromptText("活動地點");
        eventTypeField.setPromptText("活動類型");
        startTimeField.setPromptText("2026-06-12T10:00:00");
        endTimeField.setPromptText("2026-06-12T12:00:00");
        capacityField.setPromptText("30");
        descriptionField.setPromptText("活動說明");
        descriptionField.setPrefRowCount(6);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);

        form.add(new Label("標題"), 0, 0);
        form.add(titleField, 1, 0);
        form.add(new Label("地點"), 0, 1);
        form.add(locationField, 1, 1);
        form.add(new Label("類型"), 0, 2);
        form.add(eventTypeField, 1, 2);
        form.add(new Label("開始時間"), 0, 3);
        form.add(startTimeField, 1, 3);
        form.add(new Label("結束時間"), 0, 4);
        form.add(endTimeField, 1, 4);
        form.add(new Label("名額"), 0, 5);
        form.add(capacityField, 1, 5);
        form.add(new Label("說明"), 0, 6);
        form.add(descriptionField, 1, 6);

        return form;
    }

    private void refreshEvents() {
        events.setAll(context.getEventService().getAllEvents());
        refreshSelectedEventLabel();
    }

    private void fillForm(Event event) {
        if (event == null) {
            clearFormFields();
            refreshSelectedEventLabel();
            return;
        }

        titleField.setText(event.getTitle());
        locationField.setText(event.getLocation());
        eventTypeField.setText(event.getEventType());
        startTimeField.setText(event.getStartTime().format(INPUT_TIME));
        endTimeField.setText(event.getEndTime().format(INPUT_TIME));
        capacityField.setText(String.valueOf(event.getCapacity()));
        descriptionField.setText(event.getDescription());
        refreshSelectedEventLabel();
    }

    private void clearFormFields() {
        titleField.clear();
        locationField.clear();
        eventTypeField.clear();
        startTimeField.clear();
        endTimeField.clear();
        capacityField.clear();
        descriptionField.clear();
    }

    private void saveEvent() {
        try {
            Event event = buildEventFromForm();

            if (selectedEvent == null) {
                context.getEventService().addEvent(event);
                showMessage(Alert.AlertType.INFORMATION, "活動已建立");
            } else {
                context.getEventService().updateEvent(event);
                showMessage(Alert.AlertType.INFORMATION, "活動已更新");
            }

            selectedEvent = null;
            eventList.getSelectionModel().clearSelection();
            clearFormFields();
            refreshEvents();
        } catch (IllegalArgumentException exception) {
            showMessage(Alert.AlertType.WARNING, exception.getMessage());
        } catch (RuntimeException exception) {
            showMessage(Alert.AlertType.WARNING, "請確認欄位格式是否正確。時間格式範例：2026-06-12T10:00:00");
        }
    }

    private Event buildEventFromForm() {
        if (titleField.getText().trim().isEmpty()
                || locationField.getText().trim().isEmpty()
                || eventTypeField.getText().trim().isEmpty()
                || capacityField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("標題、地點、類型與名額不可空白");
        }

        LocalDateTime startTime = LocalDateTime.parse(startTimeField.getText().trim(), INPUT_TIME);
        LocalDateTime endTime = LocalDateTime.parse(endTimeField.getText().trim(), INPUT_TIME);
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("結束時間必須晚於開始時間");
        }

        int capacity = Integer.parseInt(capacityField.getText().trim());
        if (capacity <= 0) {
            throw new IllegalArgumentException("名額必須大於 0");
        }

        return new Event(
                selectedEvent == null ? 0 : selectedEvent.getEventId(),
                titleField.getText().trim(),
                descriptionField.getText().trim(),
                locationField.getText().trim(),
                eventTypeField.getText().trim(),
                startTime,
                endTime,
                organizer.getOrganizationName(),
                capacity
        );
    }

    private void deleteSelectedEvent() {
        if (selectedEvent == null) {
            showMessage(Alert.AlertType.INFORMATION, "請先選擇活動");
            return;
        }

        int eventId = selectedEvent.getEventId();
        context.getEventService().deleteEvent(eventId);
        context.getRegistrationService().cancelRegistrationsByEvent(eventId);
        showMessage(Alert.AlertType.INFORMATION, "活動已刪除，相關有效報名已取消");

        selectedEvent = null;
        eventList.getSelectionModel().clearSelection();
        clearFormFields();
        refreshEvents();
    }

    private void exportSelectedEvent() {
        if (selectedEvent == null) {
            showMessage(Alert.AlertType.INFORMATION, "請先選擇活動");
            return;
        }

        Path path = context.getCsvExporter().exportRegistrationsForEvent(selectedEvent);
        showMessage(Alert.AlertType.INFORMATION, "已匯出：" + path.toString());
    }

    private void refreshSelectedEventLabel() {
        if (selectedEvent == null) {
            selectedEventLabel.setText("請選擇活動，或直接填表建立新活動");
            return;
        }

        int registeredCount = context.getRegistrationService().countActiveRegistrations(selectedEvent.getEventId());
        selectedEventLabel.setText("目前選取：" + selectedEvent.getTitle()
                + "\n報名人數：" + registeredCount + " / " + selectedEvent.getCapacity()
                + "，狀態：" + selectedEvent.getStatus(registeredCount));
    }

    private void showMessage(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("系統提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
