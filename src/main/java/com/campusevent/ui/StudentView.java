package com.campusevent.ui;

import com.campusevent.model.Event;
import com.campusevent.model.Registration;
import com.campusevent.model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
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
    private static final int REGISTRATION_PAGE_SIZE = 5;
    private static final int REGISTRATION_RETENTION_DAYS = 30;
    private static final int REGISTRATION_LIST_HEIGHT = 260;

    private final AppContext context;
    private final Student student;
    private final ObservableList<Event> events = FXCollections.observableArrayList();
    private final ObservableList<Registration> registrationRecords = FXCollections.observableArrayList();
    private final List<Registration> recentRegistrationRecords = new ArrayList<>();
    private final ListView<Event> eventList = new ListView<>(events);
    private final ListView<Registration> registrationList = new ListView<>(registrationRecords);
    private final TextField searchField = new TextField();
    private final DatePicker datePicker = new DatePicker();
    private final ComboBox<String> typeFilter = new ComboBox<>();
    private final VBox detailContent = new VBox(10);
    private final Label detailTitleLabel = new Label("請選擇活動");
    private final HBox detailStatusRow = new HBox(8);
    private final Label detailInfoLabel = new Label();
    private final Label detailDescriptionLabel = new Label();
    private final Label eventSummaryLabel = new Label();
    private final Label registrationHintLabel = new Label();
    private final Label registrationPageLabel = new Label();
    private final Button previousRegistrationPageButton = new Button("上一頁");
    private final Button nextRegistrationPageButton = new Button("下一頁");
    private final Button registerButton = new Button("報名活動");
    private final RegistrationPager registrationPager = new RegistrationPager(REGISTRATION_PAGE_SIZE);

    public StudentView(AppContext context, Student student) {
        this.context = context;
        this.student = student;

        setPadding(new Insets(24));
        setStyle("-fx-background-color: #ffffff;");

        configureEventList();
        configureRegistrationList();

        VBox leftPane = new VBox(12, createToolbar(), eventSummaryLabel, eventList);
        VBox.setVgrow(eventList, Priority.ALWAYS);

        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setOnAction(event -> registerSelectedEvent());

        Button cancelRegistrationButton = new Button("取消選取的報名");
        cancelRegistrationButton.setMaxWidth(Double.MAX_VALUE);
        cancelRegistrationButton.setOnAction(event -> cancelSelectedRegistration());

        Label registrationTitle = new Label("我的報名紀錄");
        registrationTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2f3437;");
        registrationHintLabel.setText("* 僅顯示近 30 天報名紀錄，依報名時間由新到舊排序");
        registrationHintLabel.setWrapText(true);
        registrationHintLabel.setStyle("-fx-text-fill: #e11d48; -fx-font-size: 12px;");

        HBox registrationPager = createRegistrationPager();

        ScrollPane detailPane = createDetailPane();

        VBox rightPane = new VBox(
                12,
                detailPane,
                registerButton,
                registrationTitle,
                registrationHintLabel,
                registrationList,
                registrationPager,
                cancelRegistrationButton
        );
        rightPane.setPadding(new Insets(0, 0, 0, 24));
        rightPane.setPrefWidth(390);
        rightPane.setMinWidth(310);
        registrationList.setPrefHeight(REGISTRATION_LIST_HEIGHT);
        registrationList.setMaxHeight(REGISTRATION_LIST_HEIGHT);
        VBox.setVgrow(detailPane, Priority.ALWAYS);

        setCenter(leftPane);
        setRight(rightPane);

        loadEventTypes();
        applyFilters();
        refreshRegistrations();
    }

    private FlowPane createToolbar() {
        searchField.setPromptText("搜尋活動名稱、地點、類型...");
        searchField.setPrefWidth(260);
        searchField.setMinWidth(180);

        typeFilter.setPrefWidth(220);
        datePicker.setPromptText("日期");
        datePicker.setPrefWidth(150);

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

    private ScrollPane createDetailPane() {
        detailTitleLabel.setWrapText(true);
        detailTitleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2f3437;");
        detailInfoLabel.setWrapText(true);
        detailInfoLabel.setStyle("-fx-font-size: 14px; -fx-line-spacing: 4px; -fx-text-fill: #3e4548;");
        detailDescriptionLabel.setWrapText(true);
        detailDescriptionLabel.setStyle("-fx-font-size: 14px; -fx-line-spacing: 4px; -fx-text-fill: #3e4548;");

        detailContent.setPadding(new Insets(14));
        detailContent.setStyle("-fx-background-color: #f7f8f5; -fx-background-radius: 8;");
        detailContent.getChildren().setAll(detailTitleLabel, detailStatusRow, detailInfoLabel, detailDescriptionLabel);

        ScrollPane scrollPane = new ScrollPane(detailContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinViewportHeight(150);
        scrollPane.setPrefViewportHeight(260);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scrollPane;
    }

    private HBox createRegistrationPager() {
        previousRegistrationPageButton.setOnAction(event -> {
            registrationPager.previous();
            updateRegistrationPage();
        });

        nextRegistrationPageButton.setOnAction(event -> {
            registrationPager.next(recentRegistrationRecords.size());
            updateRegistrationPage();
        });

        HBox pager = new HBox(8, previousRegistrationPageButton, registrationPageLabel, nextRegistrationPageButton);
        pager.setAlignment(Pos.CENTER);
        return pager;
    }

    private void configureEventList() {
        eventList.setPlaceholder(new Label("找不到符合條件的活動"));
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
                boolean registeredByStudent = context.getRegistrationService()
                        .hasActiveRegistration(student.getStudentNo(), event.getEventId());
                Label title = new Label(event.getTitle());
                title.setWrapText(true);
                title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2f3437;");

                Label meta = new Label(event.getStartTime().format(DISPLAY_TIME) + " / " + event.getLocation());
                meta.setWrapText(true);
                meta.setStyle("-fx-text-fill: #536064;");

                Label type = new Label(event.getEventType());
                type.setStyle("-fx-text-fill: #536064;");

                Label count = new Label(EventStatusFormatter.registrationCount(event, registeredCount));
                count.setStyle("-fx-text-fill: #536064;");

                HBox chips = new HBox(8, StatusBadge.create(event, registeredCount, registeredByStudent), type, count);
                chips.setFillHeight(false);

                VBox cell = new VBox(5, title, meta, chips);
                cell.setPadding(new Insets(4, 0, 4, 0));
                setText(null);
                setGraphic(cell);
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
        eventSummaryLabel.setText("共 " + events.size() + " 筆活動，依活動時間由新到舊排序");

        if (!events.isEmpty()) {
            eventList.getSelectionModel().selectFirst();
        } else {
            showEventDetail(null);
        }
    }

    private void showEventDetail(Event event) {
        if (event == null) {
            detailTitleLabel.setText("請選擇活動");
            detailStatusRow.getChildren().clear();
            detailInfoLabel.setText("");
            detailDescriptionLabel.setText("");
            updateRegisterButton(null, false, 0);
            return;
        }

        int registeredCount = context.getRegistrationService().countActiveRegistrations(event.getEventId());
        boolean registeredByStudent = context.getRegistrationService()
                .hasActiveRegistration(student.getStudentNo(), event.getEventId());
        detailTitleLabel.setText(event.getTitle());
        detailStatusRow.getChildren().setAll(StatusBadge.create(event, registeredCount, registeredByStudent));
        detailInfoLabel.setText(
                "類型：" + event.getEventType()
                        + "\n" + EventStatusFormatter.registrationCountField(event, registeredCount)
                        + "\n地點：" + event.getLocation()
                        + "\n主辦單位：" + event.getOrganizerName()
                        + "\n時間：" + event.getStartTime().format(DISPLAY_TIME)
                        + " - " + event.getEndTime().format(DISPLAY_TIME)
        );
        detailDescriptionLabel.setText(event.getDescription());
        updateRegisterButton(event, registeredByStudent, registeredCount);
    }

    private void registerSelectedEvent() {
        Event event = eventList.getSelectionModel().getSelectedItem();
        if (event == null) {
            showMessage(Alert.AlertType.INFORMATION, "請先選擇活動");
            return;
        }

        if (context.getRegistrationService().hasActiveRegistration(student.getStudentNo(), event.getEventId())) {
            refreshAfterRegistrationChange(event);
            return;
        }

        try {
            context.getRegistrationService().register(student, event);
            showMessage(Alert.AlertType.INFORMATION, "報名成功");
            registrationPager.reset();
            refreshAfterRegistrationChange(event);
        } catch (IllegalArgumentException exception) {
            showMessage(Alert.AlertType.WARNING, exception.getMessage());
        }
    }

    private void updateRegisterButton(Event event, boolean registeredByStudent, int registeredCount) {
        if (event == null) {
            registerButton.setText("請選擇活動");
            registerButton.setDisable(true);
            return;
        }

        if (registeredByStudent) {
            registerButton.setText("已報名");
            registerButton.setDisable(true);
            return;
        }

        if (!event.canRegister(registeredCount)) {
            registerButton.setText(EventStatusFormatter.disabledRegisterButtonText(event, registeredCount));
            registerButton.setDisable(true);
            return;
        }

        registerButton.setText("報名活動");
        registerButton.setDisable(false);
    }

    private void refreshRegistrations() {
        recentRegistrationRecords.clear();
        recentRegistrationRecords.addAll(context.getRegistrationService()
                .getRecentRegistrationsByStudent(student.getStudentNo(), REGISTRATION_RETENTION_DAYS));
        registrationPager.clamp(recentRegistrationRecords.size());
        updateRegistrationPage();
    }

    private void updateRegistrationPage() {
        RegistrationPager.Page page = registrationPager.getPage(recentRegistrationRecords);
        registrationRecords.setAll(page.getRecords());
        registrationPageLabel.setText(page.getLabel());
        previousRegistrationPageButton.setDisable(page.isPreviousDisabled());
        nextRegistrationPageButton.setDisable(page.isNextDisabled());
    }

    private void cancelSelectedRegistration() {
        Registration registration = registrationList.getSelectionModel().getSelectedItem();
        if (registration == null) {
            showMessage(Alert.AlertType.INFORMATION, "請先選擇一筆報名紀錄");
            return;
        }

        if (!registration.isActive()) {
            showMessage(Alert.AlertType.INFORMATION,
                    RegistrationStatusFormatter.inactiveRegistrationMessage(registration));
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
                + "\n狀態：" + RegistrationStatusFormatter.displayStatus(registration.getStatus())
                + " / 報名時間：" + formatDateTime(registration.getRegisteredAt());
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
