package com.campusevent.ui;

import com.campusevent.model.Event;
import com.campusevent.model.Organizer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrganizerView extends BorderPane {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FORM_TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final String ALL_TYPES = "全部類型";
    private static final double LEFT_PANE_WIDTH = 450;
    private static final double FORM_LABEL_WIDTH = 120;
    private static final double FORM_MIN_WIDTH = 660;

    private final AppContext context;
    private final Organizer organizer;
    private final ObservableList<Event> events = FXCollections.observableArrayList();
    private final ListView<Event> eventList = new ListView<>(events);
    private final TextField searchField = new TextField();
    private final DatePicker datePicker = new DatePicker();
    private final ComboBox<String> typeFilter = new ComboBox<>();
    private final Label eventSummaryLabel = new Label();
    private final Label selectedEventLabel = new Label("請選擇活動，或直接填表建立新活動");
    private final HBox selectedStatusRow = new HBox(8);

    private final TextField titleField = new TextField();
    private final TextField locationField = new TextField();
    private final ComboBox<String> eventTypeComboBox = new ComboBox<>();
    private final DatePicker startDatePicker = new DatePicker();
    private final TextField startTimeField = new TextField();
    private final DatePicker endDatePicker = new DatePicker();
    private final TextField endTimeField = new TextField();
    private final TextField capacityField = new TextField();
    private final TextArea descriptionField = new TextArea();
    private final HBox formActions = new HBox(10);
    private final Button saveButton = new Button("儲存活動");
    private final Button cancelButton = new Button("取消");
    private final Button editButton = new Button("編輯");
    private final Button deleteButton = new Button("刪除");

    private Event selectedEvent;
    private FormMode formMode = FormMode.CREATE;

    public OrganizerView(AppContext context, Organizer organizer) {
        this.context = context;
        this.organizer = organizer;

        setPadding(new Insets(24));
        setStyle("-fx-background-color: #ffffff;");

        configureEventList();
        loadEventTypes();
        configureActionButtons();

        Label title = new Label("主辦者活動管理");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2f3437;");

        VBox formPane = new VBox(14, selectedEventLabel, selectedStatusRow, new Separator(), createForm(), formActions);
        formPane.setPadding(new Insets(0, 0, 0, 24));
        formPane.setMinWidth(FORM_MIN_WIDTH);

        VBox leftPane = new VBox(12, title, createToolbar(), eventSummaryLabel, eventList);
        leftPane.setMinWidth(LEFT_PANE_WIDTH);
        leftPane.setPrefWidth(LEFT_PANE_WIDTH);
        leftPane.setMaxWidth(LEFT_PANE_WIDTH);
        VBox.setVgrow(eventList, Priority.ALWAYS);

        setLeft(leftPane);
        setCenter(formPane);

        refreshEvents();
        enterCreateMode();
    }

    private void configureActionButtons() {
        formActions.setAlignment(Pos.CENTER_RIGHT);

        saveButton.setDefaultButton(true);
        saveButton.setStyle("-fx-background-color: #6f8f8b; -fx-text-fill: white; -fx-font-weight: bold;");
        saveButton.setOnAction(event -> saveEvent());

        cancelButton.setOnAction(event -> cancelFormAction());
        editButton.setOnAction(event -> enterEditMode());
        deleteButton.setOnAction(event -> deleteSelectedEvent());
    }

    private void enterCreateMode() {
        formMode = FormMode.CREATE;
        selectedEvent = null;
        eventList.getSelectionModel().clearSelection();
        clearFormFields();
        selectedEventLabel.setText("建立新活動");
        selectedStatusRow.getChildren().clear();
        setFormEditable(true);
        updateFormActions();
    }

    private void enterViewMode(Event event) {
        formMode = FormMode.VIEW;
        selectedEvent = event;
        fillForm(event);
        refreshSelectedEventLabel();
        setFormEditable(false);
        updateFormActions();
    }

    private void enterEditMode() {
        if (selectedEvent == null) {
            enterCreateMode();
            return;
        }

        formMode = FormMode.EDIT;
        selectedEventLabel.setText("編輯活動：" + selectedEvent.getTitle());
        setFormEditable(true);
        updateFormActions();
    }

    private void cancelFormAction() {
        if (formMode == FormMode.EDIT && selectedEvent != null) {
            enterViewMode(selectedEvent);
            return;
        }

        enterCreateMode();
    }

    private void updateFormActions() {
        formActions.getChildren().clear();
        if (formMode == FormMode.CREATE) {
            saveButton.setText("儲存活動");
            formActions.getChildren().setAll(saveButton);
        } else if (formMode == FormMode.VIEW) {
            formActions.getChildren().setAll(cancelButton, editButton, deleteButton);
        } else {
            saveButton.setText("儲存變更");
            formActions.getChildren().setAll(cancelButton, saveButton);
        }
    }

    private void setFormEditable(boolean editable) {
        titleField.setEditable(editable);
        titleField.setDisable(!editable);
        locationField.setEditable(editable);
        locationField.setDisable(!editable);
        eventTypeComboBox.setDisable(!editable);
        startDatePicker.setDisable(!editable);
        startTimeField.setEditable(editable);
        startTimeField.setDisable(!editable);
        endDatePicker.setDisable(!editable);
        endTimeField.setEditable(editable);
        endTimeField.setDisable(!editable);
        capacityField.setEditable(editable);
        capacityField.setDisable(!editable);
        descriptionField.setEditable(editable);
        descriptionField.setDisable(!editable);
    }

    private FlowPane createToolbar() {
        searchField.setPromptText("搜尋活動名稱、地點、類型...");
        searchField.setPrefWidth(220);
        searchField.setMinWidth(180);

        datePicker.setPromptText("日期");
        datePicker.setPrefWidth(145);

        typeFilter.setPrefWidth(240);

        Button searchButton = new Button("查詢");
        searchButton.setOnAction(event -> applyFilters());

        Button resetButton = new Button("清除");
        resetButton.setOnAction(event -> {
            searchField.clear();
            datePicker.setValue(null);
            typeFilter.getSelectionModel().select(ALL_TYPES);
            applyFilters();
        });

        FlowPane toolbar = new FlowPane(Orientation.HORIZONTAL, 10, 10);
        toolbar.setPadding(new Insets(0, 0, 8, 0));
        toolbar.getChildren().addAll(searchField, datePicker, typeFilter, searchButton, resetButton);
        return toolbar;
    }

    private void configureEventList() {
        eventList.setPlaceholder(new Label("目前沒有活動"));
        eventList.setCellFactory(listView -> new ListCell<Event>() {
            @Override
            protected void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);
                if (empty || event == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                int registeredCount = context.getRegistrationService().countActiveRegistrations(event.getEventId());
                Label title = new Label(event.getTitle());
                title.setWrapText(true);
                title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2f3437;");

                Label meta = new Label(event.getStartTime().format(DISPLAY_TIME) + " / " + event.getLocation());
                meta.setWrapText(true);
                meta.setStyle("-fx-text-fill: #536064;");

                Label count = new Label(formatRegistrationCount(event, registeredCount));
                count.setStyle("-fx-text-fill: #536064;");

                HBox chips = new HBox(8, StatusBadge.create(event, registeredCount), count);
                chips.setFillHeight(false);

                VBox cell = new VBox(5, title, meta, chips);
                cell.setPadding(new Insets(4, 0, 4, 0));
                setText(null);
                setGraphic(cell);
            }
        });

        eventList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                if (formMode != FormMode.CREATE) {
                    enterCreateMode();
                }
                return;
            }

            enterViewMode(newValue);
        });
    }

    private GridPane createForm() {
        titleField.setPromptText("活動標題");
        titleField.setMaxWidth(Double.MAX_VALUE);
        locationField.setPromptText("活動地點");
        locationField.setMaxWidth(Double.MAX_VALUE);
        eventTypeComboBox.setPromptText("請選擇活動類型");
        eventTypeComboBox.setMaxWidth(Double.MAX_VALUE);
        startDatePicker.setPromptText("開始日期");
        startDatePicker.setPrefWidth(180);
        startTimeField.setPromptText("HH:mm");
        startTimeField.setPrefWidth(110);
        endDatePicker.setPromptText("結束日期");
        endDatePicker.setPrefWidth(180);
        endTimeField.setPromptText("HH:mm");
        endTimeField.setPrefWidth(110);
        capacityField.setPromptText("0");
        capacityField.setText("0");
        capacityField.setMaxWidth(Double.MAX_VALUE);
        descriptionField.setPromptText("活動說明");
        descriptionField.setPrefRowCount(6);
        descriptionField.setMaxWidth(Double.MAX_VALUE);

        Label capacityHint = new Label("* 預設 0 代表無人數限制");
        capacityHint.setStyle("-fx-text-fill: #e11d48; -fx-font-size: 12px;");
        VBox capacityBox = new VBox(4, capacityField, capacityHint);
        capacityBox.setMaxWidth(Double.MAX_VALUE);
        HBox startDateTimeBox = new HBox(8, startDatePicker, startTimeField);
        HBox endDateTimeBox = new HBox(8, endDatePicker, endTimeField);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setMinWidth(FORM_MIN_WIDTH);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(FORM_LABEL_WIDTH);
        labelColumn.setPrefWidth(FORM_LABEL_WIDTH);
        labelColumn.setHalignment(HPos.LEFT);

        ColumnConstraints inputColumn = new ColumnConstraints();
        inputColumn.setHgrow(Priority.ALWAYS);
        inputColumn.setFillWidth(true);
        form.getColumnConstraints().addAll(labelColumn, inputColumn);

        Label titleLabel = createFormLabel("標題");
        Label locationLabel = createFormLabel("地點");
        Label typeLabel = createFormLabel("類型");
        Label startTimeLabel = createFormLabel("開始時間");
        Label endTimeLabel = createFormLabel("結束時間");
        Label capacityLabel = createFormLabel("報名限制人數");
        Label descriptionLabel = createFormLabel("說明");

        form.add(titleLabel, 0, 0);
        form.add(titleField, 1, 0);
        form.add(locationLabel, 0, 1);
        form.add(locationField, 1, 1);
        form.add(typeLabel, 0, 2);
        form.add(eventTypeComboBox, 1, 2);
        form.add(startTimeLabel, 0, 3);
        form.add(startDateTimeBox, 1, 3);
        form.add(endTimeLabel, 0, 4);
        form.add(endDateTimeBox, 1, 4);
        form.add(capacityLabel, 0, 5);
        form.add(capacityBox, 1, 5);
        form.add(descriptionLabel, 0, 6);
        form.add(descriptionField, 1, 6);
        GridPane.setValignment(descriptionLabel, VPos.TOP);

        return form;
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setMinWidth(FORM_LABEL_WIDTH);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #2f3437;");
        return label;
    }

    private void loadEventTypes() {
        List<String> structuredTypes = context.getEventService().getEventTypes();
        List<String> filterTypes = new ArrayList<>();
        filterTypes.add(ALL_TYPES);
        filterTypes.addAll(structuredTypes);

        typeFilter.setItems(FXCollections.observableArrayList(filterTypes));
        typeFilter.getSelectionModel().select(ALL_TYPES);
        eventTypeComboBox.setItems(FXCollections.observableArrayList(structuredTypes));
    }

    private void refreshEvents() {
        applyFilters();
    }

    private void applyFilters() {
        Event eventToRestore = selectedEvent;
        LocalDate selectedDate = datePicker.getValue();
        String selectedType = typeFilter.getSelectionModel().getSelectedItem();
        if (ALL_TYPES.equals(selectedType)) {
            selectedType = "";
        }

        events.setAll(context.getEventService().findEvents(searchField.getText(), selectedDate, selectedType));
        eventSummaryLabel.setText("共 " + events.size() + " 筆活動，依活動時間由新到舊排序");

        if (eventToRestore == null) {
            eventList.getSelectionModel().clearSelection();
            if (formMode == FormMode.CREATE) {
                selectedEventLabel.setText("建立新活動");
                selectedStatusRow.getChildren().clear();
                updateFormActions();
            } else {
                enterCreateMode();
            }
            return;
        }

        events.stream()
                .filter(event -> event.getEventId() == eventToRestore.getEventId())
                .findFirst()
                .ifPresentOrElse(
                        event -> eventList.getSelectionModel().select(event),
                        () -> {
                            enterCreateMode();
                        }
                );
    }

    private void fillForm(Event event) {
        if (event == null) {
            clearFormFields();
            refreshSelectedEventLabel();
            return;
        }

        titleField.setText(event.getTitle());
        locationField.setText(event.getLocation());
        if (!eventTypeComboBox.getItems().contains(event.getEventType())) {
            eventTypeComboBox.getItems().add(event.getEventType());
        }
        eventTypeComboBox.getSelectionModel().select(event.getEventType());
        startDatePicker.setValue(event.getStartTime().toLocalDate());
        startTimeField.setText(event.getStartTime().toLocalTime().format(FORM_TIME));
        endDatePicker.setValue(event.getEndTime().toLocalDate());
        endTimeField.setText(event.getEndTime().toLocalTime().format(FORM_TIME));
        capacityField.setText(String.valueOf(event.getCapacity()));
        descriptionField.setText(event.getDescription());
        refreshSelectedEventLabel();
    }

    private void clearFormFields() {
        titleField.clear();
        locationField.clear();
        eventTypeComboBox.getSelectionModel().clearSelection();
        startDatePicker.setValue(null);
        startTimeField.clear();
        endDatePicker.setValue(null);
        endTimeField.clear();
        capacityField.setText("0");
        descriptionField.clear();
    }

    private void saveEvent() {
        try {
            boolean creating = formMode == FormMode.CREATE || selectedEvent == null;
            Event event = buildEventFromForm();

            if (creating) {
                context.getEventService().addEvent(event);
                showMessage(Alert.AlertType.INFORMATION, "活動已建立");
                refreshEvents();
                enterCreateMode();
            } else {
                context.getEventService().updateEvent(event);
                showMessage(Alert.AlertType.INFORMATION, "活動已更新");
                selectedEvent = event;
                formMode = FormMode.VIEW;
                refreshEvents();
            }
        } catch (IllegalArgumentException exception) {
            showMessage(Alert.AlertType.WARNING, exception.getMessage());
        } catch (RuntimeException exception) {
            showMessage(Alert.AlertType.WARNING, "請確認欄位格式是否正確。時間格式範例：10:00");
        }
    }

    private Event buildEventFromForm() {
        String selectedType = eventTypeComboBox.getSelectionModel().getSelectedItem();
        if (titleField.getText().trim().isEmpty()
                || locationField.getText().trim().isEmpty()
                || selectedType == null
                || selectedType.trim().isEmpty()) {
            throw new IllegalArgumentException("標題、地點與活動類型不可空白");
        }

        LocalDateTime startTime = buildDateTime(startDatePicker, startTimeField, "開始時間");
        LocalDateTime endTime = buildDateTime(endDatePicker, endTimeField, "結束時間");
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("結束時間必須晚於開始時間");
        }

        int capacity = parseCapacityLimit();
        if (capacity < 0) {
            throw new IllegalArgumentException("報名限制人數不可小於 0");
        }

        return new Event(
                selectedEvent == null ? 0 : selectedEvent.getEventId(),
                titleField.getText().trim(),
                descriptionField.getText().trim(),
                locationField.getText().trim(),
                selectedType.trim(),
                startTime,
                endTime,
                organizer.getOrganizationName(),
                capacity
        );
    }

    private LocalDateTime buildDateTime(DatePicker datePicker, TextField timeField, String fieldName) {
        LocalDate date = datePicker.getValue();
        String timeText = timeField.getText().trim();
        if (date == null || timeText.isEmpty()) {
            throw new IllegalArgumentException(fieldName + "的日期與時間不可空白");
        }

        try {
            LocalTime time = LocalTime.parse(timeText, DateTimeFormatter.ISO_LOCAL_TIME);
            return LocalDateTime.of(date, time);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(fieldName + "的時間格式請輸入 HH:mm，例如 10:00");
        }
    }

    private int parseCapacityLimit() {
        String value = capacityField.getText().trim();
        if (value.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("報名限制人數必須是 0 或正整數");
        }
    }

    private void deleteSelectedEvent() {
        if (selectedEvent == null) {
            showMessage(Alert.AlertType.INFORMATION, "請先選擇活動");
            return;
        }

        int eventId = selectedEvent.getEventId();
        int activeRegistrationCount = context.getRegistrationService().countActiveRegistrations(eventId);
        if (!confirmDeleteEvent(activeRegistrationCount)) {
            return;
        }

        context.getEventService().deleteEvent(eventId);
        if (activeRegistrationCount > 0) {
            context.getRegistrationService().markRegistrationsDeletedByEvent(eventId);
            showMessage(Alert.AlertType.INFORMATION, "活動已刪除，學生報名紀錄已標示為活動已刪除");
        } else {
            showMessage(Alert.AlertType.INFORMATION, "活動已刪除");
        }

        selectedEvent = null;
        refreshEvents();
        enterCreateMode();
    }

    private boolean confirmDeleteEvent(int activeRegistrationCount) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("刪除活動確認");
        alert.setHeaderText("確定要刪除「" + selectedEvent.getTitle() + "」嗎？");
        if (activeRegistrationCount > 0) {
            alert.setContentText("目前有 " + activeRegistrationCount
                    + " 筆有效報名。刪除後，學生的報名紀錄會標示為「活動已刪除」。");
        } else {
            alert.setContentText("目前沒有有效報名，確認後會直接刪除此活動。");
        }

        ButtonType deleteButton = new ButtonType("刪除");
        alert.getButtonTypes().setAll(deleteButton, ButtonType.CANCEL);
        return alert.showAndWait()
                .filter(buttonType -> buttonType == deleteButton)
                .isPresent();
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
            selectedEventLabel.setText("建立新活動");
            selectedStatusRow.getChildren().clear();
            return;
        }

        int registeredCount = context.getRegistrationService().countActiveRegistrations(selectedEvent.getEventId());
        selectedEventLabel.setText("目前選取：" + selectedEvent.getTitle());
        selectedStatusRow.getChildren().setAll(
                StatusBadge.create(selectedEvent, registeredCount),
                new Label(formatRegistrationCount(selectedEvent, registeredCount))
        );
    }

    private String formatRegistrationCount(Event event, int registeredCount) {
        return "報名 " + registeredCount + " / " + event.getCapacityText();
    }

    private void showMessage(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("系統提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private enum FormMode {
        CREATE,
        VIEW,
        EDIT
    }
}
