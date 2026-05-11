package com.project.artconnect.ui;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.ExhibitionService;
import com.project.artconnect.service.GalleryService;
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
import java.util.ArrayList;
import java.util.List;

public class ExhibitionController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Gallery> galleryFilter;
    @FXML
    private TextField titleInput;
    @FXML
    private DatePicker startDateInput;
    @FXML
    private DatePicker endDateInput;
    @FXML
    private TextField themeInput;
    @FXML
    private TextField curatorInput;
    @FXML
    private TextArea descriptionInput;
    @FXML
    private ComboBox<Gallery> galleryInput;
    @FXML
    private TableView<Exhibition> exhibitionTable;
    @FXML
    private TableColumn<Exhibition, String> titleColumn;
    @FXML
    private TableColumn<Exhibition, LocalDate> startDateColumn;
    @FXML
    private TableColumn<Exhibition, LocalDate> endDateColumn;
    @FXML
    private TableColumn<Exhibition, String> themeColumn;
    @FXML
    private TableColumn<Exhibition, String> curatorColumn;
    @FXML
    private TableColumn<Exhibition, String> descriptionPreviewColumn;
    @FXML
    private TableColumn<Exhibition, String> galleryColumn;

    private final ExhibitionService exhibitionService = ServiceProvider.getExhibitionService();
    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        themeColumn.setCellValueFactory(new PropertyValueFactory<>("theme"));
        curatorColumn.setCellValueFactory(new PropertyValueFactory<>("curatorName"));
        descriptionPreviewColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
            preview(cellData.getValue() != null ? cellData.getValue().getDescription() : null, 70)));
        galleryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getGallery() != null ? cellData.getValue().getGallery().getName() : "Unknown"));

        exhibitionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });

        refreshGalleries();
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        Gallery selectedGallery = galleryFilter.getValue();
        exhibitionTable.setItems(FXCollections.observableArrayList(exhibitionService.getAllExhibitions().stream()
                .filter(exhibition -> query.isEmpty()
                        || (exhibition.getTitle() != null
                                && exhibition.getTitle().toLowerCase().contains(query)))
                .filter(exhibition -> selectedGallery == null
                        || (exhibition.getGallery() != null
                                && selectedGallery.getName().equalsIgnoreCase(exhibition.getGallery().getName())))
                .toList()));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        galleryFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleAddExhibition() {
        Exhibition exhibition = buildExhibitionFromForm();
        if (exhibition == null) {
            return;
        }
        try {
            exhibitionService.createExhibition(exhibition);
            refreshTable();
            selectByTitle(exhibition.getTitle());
        } catch (RuntimeException e) {
            showError("Failed to create exhibition: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateExhibition() {
        Exhibition selected = exhibitionTable.getSelectionModel().getSelectedItem();
        Exhibition exhibition = buildExhibitionFromForm();
        if (selected == null || exhibition == null) {
            showInfo("Select an exhibition to update.");
            return;
        }
        if (!selected.getTitle().equals(exhibition.getTitle())) {
            showInfo("Update requires the same exhibition title as the selected row.");
            return;
        }
        exhibition.setArtworks(new ArrayList<>(selected.getArtworks()));
        try {
            exhibitionService.updateExhibition(exhibition);
            refreshTable();
            selectByTitle(exhibition.getTitle());
        } catch (RuntimeException e) {
            showError("Failed to update exhibition: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteExhibition() {
        Exhibition selected = exhibitionTable.getSelectionModel().getSelectedItem();
        String targetTitle = selected != null ? selected.getTitle() : titleInput.getText();
        if (targetTitle == null || targetTitle.isBlank()) {
            showInfo("Select an exhibition or enter a title to delete.");
            return;
        }
        try {
            exhibitionService.deleteExhibition(targetTitle.trim());
            refreshTable();
            handleClearForm();
        } catch (RuntimeException e) {
            showError("Failed to delete exhibition: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        titleInput.clear();
        startDateInput.setValue(null);
        endDateInput.setValue(null);
        themeInput.clear();
        curatorInput.clear();
        descriptionInput.clear();
        galleryInput.setValue(null);
        exhibitionTable.getSelectionModel().clearSelection();
    }

    private void refreshTable() {
        exhibitionTable.setItems(FXCollections.observableArrayList(exhibitionService.getAllExhibitions()));
    }

    private void refreshGalleries() {
        List<Gallery> galleries = galleryService.getAllGalleries();
        galleryFilter.setItems(FXCollections.observableArrayList(galleries));
        galleryInput.setItems(FXCollections.observableArrayList(galleries));
    }

    private Exhibition buildExhibitionFromForm() {
        String title = titleInput.getText() != null ? titleInput.getText().trim() : "";
        if (title.isEmpty()) {
            showInfo("Title is required.");
            return null;
        }
        LocalDate startDate = startDateInput.getValue();
        LocalDate endDate = endDateInput.getValue();
        if (startDate == null || endDate == null) {
            showInfo("Start and end dates are required.");
            return null;
        }
        if (endDate.isBefore(startDate)) {
            showInfo("End date must be on or after start date.");
            return null;
        }
        Gallery gallery = galleryInput.getValue();
        if (gallery == null) {
            showInfo("Gallery selection is required.");
            return null;
        }

        Exhibition exhibition = new Exhibition();
        exhibition.setTitle(title);
        exhibition.setStartDate(startDate);
        exhibition.setEndDate(endDate);
        exhibition.setTheme(themeInput.getText() != null ? themeInput.getText().trim() : null);
        exhibition.setCuratorName(curatorInput.getText() != null ? curatorInput.getText().trim() : null);
        exhibition.setDescription(descriptionInput.getText() != null ? descriptionInput.getText().trim() : null);
        exhibition.setGallery(gallery);
        return exhibition;
    }

    private void populateForm(Exhibition exhibition) {
        titleInput.setText(exhibition.getTitle());
        startDateInput.setValue(exhibition.getStartDate());
        endDateInput.setValue(exhibition.getEndDate());
        themeInput.setText(exhibition.getTheme());
        curatorInput.setText(exhibition.getCuratorName());
        descriptionInput.setText(exhibition.getDescription());
        galleryInput.setValue(exhibition.getGallery());
    }

    private void selectByTitle(String title) {
        if (title == null) {
            return;
        }
        for (Exhibition exhibition : exhibitionTable.getItems()) {
            if (title.equals(exhibition.getTitle())) {
                exhibitionTable.getSelectionModel().select(exhibition);
                break;
            }
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exhibitions");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exhibitions");
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
