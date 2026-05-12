package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class WorkshopController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> levelFilter;
    @FXML
    private TextField titleInput;
    @FXML
    private DatePicker dateInput;
    @FXML
    private TextField timeInput;
    @FXML
    private TextField durationInput;
    @FXML
    private TextField maxParticipantsInput;
    @FXML
    private TextField priceInput;
    @FXML
    private TextField locationInput;
    @FXML
    private ComboBox<String> levelInput;
    @FXML
    private ComboBox<Artist> instructorInput;
    @FXML
    private TextArea descriptionInput;
    @FXML
    private TableView<Workshop> workshopTable;
    @FXML
    private TableColumn<Workshop, String> titleColumn;
    @FXML
    private TableColumn<Workshop, String> dateColumn;
    @FXML
    private TableColumn<Workshop, String> instructorColumn;
    @FXML
    private TableColumn<Workshop, Double> priceColumn;
    @FXML
    private TableColumn<Workshop, String> levelColumn;
    @FXML
    private TableColumn<Workshop, Integer> maxParticipantsColumn;
    @FXML
    private TableColumn<Workshop, String> locationColumn;
    @FXML
    private TableColumn<Workshop, Integer> durationColumn;
    @FXML
    private TableColumn<Workshop, String> descriptionPreviewColumn;

    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime dt = cellData.getValue() != null ? cellData.getValue().getDate() : null;
            String formatted = dt != null ? dt.format(DATE_TIME_FORMAT) : "";
            return new SimpleStringProperty(formatted);
        });
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        maxParticipantsColumn.setCellValueFactory(new PropertyValueFactory<>("maxParticipants"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        descriptionPreviewColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
            preview(cellData.getValue() != null ? cellData.getValue().getDescription() : null, 65)));

        instructorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getInstructor() != null ? cellData.getValue().getInstructor().getName()
                        : "Unknown"));

        levelInput.setItems(FXCollections.observableArrayList("beginner", "intermediate", "advanced"));
        levelFilter.setItems(FXCollections.observableArrayList("beginner", "intermediate", "advanced"));
        instructorInput.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));

        workshopTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });

        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String level = levelFilter.getValue();
        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops().stream()
                .filter(workshop -> query.isEmpty()
                        || (workshop.getTitle() != null && workshop.getTitle().toLowerCase().contains(query))
                        || (workshop.getInstructor() != null && workshop.getInstructor().getName() != null
                                && workshop.getInstructor().getName().toLowerCase().contains(query)))
                .filter(workshop -> level == null || level.equalsIgnoreCase(workshop.getLevel()))
                .toList()));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        levelFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleAddWorkshop() {
        Workshop workshop = buildWorkshopFromForm();
        if (workshop == null) {
            return;
        }
        try {
            workshopService.createWorkshop(workshop);
            refreshTable();
            selectByTitle(workshop.getTitle());
        } catch (RuntimeException e) {
            showError("Failed to create workshop: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateWorkshop() {
        Workshop selected = workshopTable.getSelectionModel().getSelectedItem();
        Workshop workshop = buildWorkshopFromForm();
        if (selected == null || workshop == null) {
            showInfo("Select a workshop to update.");
            return;
        }
        if (!selected.getTitle().equals(workshop.getTitle())) {
            showInfo("Update requires the same workshop title as the selected row.");
            return;
        }
        try {
            workshopService.updateWorkshop(workshop);
            refreshTable();
            selectByTitle(workshop.getTitle());
        } catch (RuntimeException e) {
            showError("Failed to update workshop: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteWorkshop() {
        Workshop selected = workshopTable.getSelectionModel().getSelectedItem();
        String targetTitle = selected != null ? selected.getTitle() : titleInput.getText();
        if (targetTitle == null || targetTitle.isBlank()) {
            showInfo("Select a workshop or enter a title to delete.");
            return;
        }
        try {
            workshopService.deleteWorkshop(targetTitle.trim());
            refreshTable();
            handleClearForm();
        } catch (RuntimeException e) {
            showError("Failed to delete workshop: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        titleInput.clear();
        dateInput.setValue(null);
        timeInput.clear();
        durationInput.clear();
        maxParticipantsInput.clear();
        priceInput.clear();
        locationInput.clear();
        levelInput.setValue(null);
        instructorInput.setValue(null);
        descriptionInput.clear();
        workshopTable.getSelectionModel().clearSelection();
    }

    private void refreshTable() {
        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
    }

    private Workshop buildWorkshopFromForm() {
        String title = titleInput.getText() != null ? titleInput.getText().trim() : "";
        if (title.isEmpty()) {
            showInfo("Title is required.");
            return null;
        }
        LocalDate date = dateInput.getValue();
        if (date == null) {
            showInfo("Date is required.");
            return null;
        }
        LocalTime time = parseTime(timeInput.getText());
        if (time == null) {
            showInfo("Time is required (HH:mm).");
            return null;
        }
        Artist instructor = instructorInput.getValue();
        if (instructor == null) {
            showInfo("Instructor selection is required.");
            return null;
        }

        Integer duration = parseInteger(durationInput.getText(), "Duration minutes");
        Integer maxParticipants = parseInteger(maxParticipantsInput.getText(), "Max participants");
        Double price = parseDouble(priceInput.getText(), "Price");
        if (duration == null || maxParticipants == null || price == null) {
            return null;
        }

        String level = levelInput.getValue();
        if (level == null) {
            showInfo("Level is required.");
            return null;
        }

        Workshop workshop = new Workshop();
        workshop.setTitle(title);
        workshop.setDate(LocalDateTime.of(date, time));
        workshop.setDurationMinutes(duration);
        workshop.setMaxParticipants(maxParticipants);
        workshop.setPrice(price);
        workshop.setLocation(locationInput.getText() != null ? locationInput.getText().trim() : null);
        workshop.setDescription(descriptionInput.getText() != null ? descriptionInput.getText().trim() : null);
        workshop.setLevel(level);
        workshop.setInstructor(instructor);
        return workshop;
    }

    private void populateForm(Workshop workshop) {
        titleInput.setText(workshop.getTitle());
        if (workshop.getDate() != null) {
            dateInput.setValue(workshop.getDate().toLocalDate());
            timeInput.setText(workshop.getDate().toLocalTime().format(TIME_FORMAT));
        } else {
            dateInput.setValue(null);
            timeInput.clear();
        }
        durationInput.setText(String.valueOf(workshop.getDurationMinutes()));
        maxParticipantsInput.setText(String.valueOf(workshop.getMaxParticipants()));
        priceInput.setText(String.valueOf(workshop.getPrice()));
        locationInput.setText(workshop.getLocation());
        descriptionInput.setText(workshop.getDescription());
        levelInput.setValue(workshop.getLevel());
        instructorInput.setValue(workshop.getInstructor());
    }

    private void selectByTitle(String title) {
        if (title == null) {
            return;
        }
        for (Workshop workshop : workshopTable.getItems()) {
            if (title.equals(workshop.getTitle())) {
                workshopTable.getSelectionModel().select(workshop);
                break;
            }
        }
    }

    private LocalTime parseTime(String value) {
        String trimmed = value != null ? value.trim() : "";
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(trimmed, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Integer parseInteger(String value, String label) {
        String trimmed = value != null ? value.trim() : "";
        if (trimmed.isEmpty()) {
            showInfo(label + " is required.");
            return null;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            showInfo(label + " must be a number.");
            return null;
        }
    }

    private Double parseDouble(String value, String label) {
        String trimmed = value != null ? value.trim() : "";
        if (trimmed.isEmpty()) {
            showInfo(label + " is required.");
            return null;
        }
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException e) {
            showInfo(label + " must be a number.");
            return null;
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Workshops");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Workshops");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String preview(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String trimmed = text.trim().replace('\n', ' ');
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength - 1) + "...";
    }
}
