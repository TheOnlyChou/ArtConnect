package com.project.artconnect.ui;

import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class GalleryController {
    @FXML
    private TextField searchField;
    @FXML
    private TextField nameInput;
    @FXML
    private TextField addressInput;
    @FXML
    private TextField ownerInput;
    @FXML
    private TextField hoursInput;
    @FXML
    private TextField phoneInput;
    @FXML
    private TextField ratingInput;
    @FXML
    private TextField websiteInput;
    @FXML
    private TableView<Gallery> galleryTable;
    @FXML
    private TableColumn<Gallery, String> nameColumn;
    @FXML
    private TableColumn<Gallery, String> addressColumn;
    @FXML
    private TableColumn<Gallery, String> ownerColumn;
    @FXML
    private TableColumn<Gallery, String> phoneColumn;
    @FXML
    private TableColumn<Gallery, Double> ratingColumn;
    @FXML
    private TableColumn<Gallery, String> hoursColumn;
    @FXML
    private TableColumn<Gallery, String> websiteColumn;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("contactPhone"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        hoursColumn.setCellValueFactory(new PropertyValueFactory<>("openingHours"));
        websiteColumn.setCellValueFactory(new PropertyValueFactory<>("website"));

        galleryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        galleryTable.setItems(FXCollections.observableArrayList(galleryService.getAllGalleries().stream()
                .filter(gallery -> query.isEmpty()
                        || (gallery.getName() != null && gallery.getName().toLowerCase().contains(query))
                        || (gallery.getAddress() != null && gallery.getAddress().toLowerCase().contains(query))
                        || (gallery.getOwnerName() != null && gallery.getOwnerName().toLowerCase().contains(query)))
                .toList()));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        refreshTable();
    }

    @FXML
    private void handleAddGallery() {
        Gallery gallery = buildGalleryFromForm();
        if (gallery == null) {
            return;
        }
        try {
            galleryService.createGallery(gallery);
            refreshTable();
            selectByName(gallery.getName());
        } catch (RuntimeException e) {
            showError("Failed to create gallery: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateGallery() {
        Gallery selected = galleryTable.getSelectionModel().getSelectedItem();
        Gallery gallery = buildGalleryFromForm();
        if (selected == null || gallery == null) {
            showInfo("Select a gallery to update.");
            return;
        }
        if (!selected.getName().equals(gallery.getName())) {
            showInfo("Update requires the same gallery name as the selected row.");
            return;
        }
        try {
            galleryService.updateGallery(gallery);
            refreshTable();
            selectByName(gallery.getName());
        } catch (RuntimeException e) {
            showError("Failed to update gallery: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteGallery() {
        Gallery selected = galleryTable.getSelectionModel().getSelectedItem();
        String targetName = selected != null ? selected.getName() : nameInput.getText();
        if (targetName == null || targetName.isBlank()) {
            showInfo("Select a gallery or enter a name to delete.");
            return;
        }
        try {
            galleryService.deleteGallery(targetName.trim());
            refreshTable();
            handleClearForm();
        } catch (RuntimeException e) {
            showError("Failed to delete gallery: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        nameInput.clear();
        addressInput.clear();
        ownerInput.clear();
        hoursInput.clear();
        phoneInput.clear();
        ratingInput.clear();
        websiteInput.clear();
        galleryTable.getSelectionModel().clearSelection();
    }

    private void refreshTable() {
        galleryTable.setItems(FXCollections.observableArrayList(galleryService.getAllGalleries()));
    }

    private Gallery buildGalleryFromForm() {
        String name = nameInput.getText() != null ? nameInput.getText().trim() : "";
        if (name.isEmpty()) {
            showInfo("Name is required.");
            return null;
        }

        Gallery gallery = new Gallery();
        gallery.setName(name);
        gallery.setAddress(addressInput.getText() != null ? addressInput.getText().trim() : null);
        gallery.setOwnerName(ownerInput.getText() != null ? ownerInput.getText().trim() : null);
        gallery.setOpeningHours(hoursInput.getText() != null ? hoursInput.getText().trim() : null);
        gallery.setContactPhone(phoneInput.getText() != null ? phoneInput.getText().trim() : null);
        gallery.setWebsite(websiteInput.getText() != null ? websiteInput.getText().trim() : null);

        String ratingText = ratingInput.getText() != null ? ratingInput.getText().trim() : "";
        if (!ratingText.isEmpty()) {
            try {
                gallery.setRating(Double.parseDouble(ratingText));
            } catch (NumberFormatException e) {
                showInfo("Rating must be a number.");
                return null;
            }
        } else {
            gallery.setRating(0.0);
        }
        return gallery;
    }

    private void populateForm(Gallery gallery) {
        nameInput.setText(gallery.getName());
        addressInput.setText(gallery.getAddress());
        ownerInput.setText(gallery.getOwnerName());
        hoursInput.setText(gallery.getOpeningHours());
        phoneInput.setText(gallery.getContactPhone());
        ratingInput.setText(String.valueOf(gallery.getRating()));
        websiteInput.setText(gallery.getWebsite());
    }

    private void selectByName(String name) {
        if (name == null) {
            return;
        }
        for (Gallery gallery : galleryTable.getItems()) {
            if (name.equals(gallery.getName())) {
                galleryTable.getSelectionModel().select(gallery);
                break;
            }
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Galleries");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Galleries");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
