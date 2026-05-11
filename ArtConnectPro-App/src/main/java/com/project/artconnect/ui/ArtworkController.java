package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.Arrays;

public class ArtworkController {
    @FXML
    private TableView<Artwork> artworkTable;
    @FXML
    private TableColumn<Artwork, String> titleColumn;
    @FXML
    private TableColumn<Artwork, String> typeColumn;
    @FXML
    private TableColumn<Artwork, Integer> yearColumn;
    @FXML
    private TableColumn<Artwork, Double> priceColumn;
    @FXML
    private TableColumn<Artwork, String> statusColumn;
    @FXML
    private TableColumn<Artwork, String> artistColumn;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    private TextField titleInput;
    @FXML
    private ComboBox<Artist> artistCombo;
    @FXML
    private TextField typeInput;
    @FXML
    private TextField priceInput;
    @FXML
    private TextField yearInput;
    @FXML
    private ComboBox<Artwork.Status> statusCombo;
    @FXML
    private TextField searchField;

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("creationYear"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        artistColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getArtist() != null ? cellData.getValue().getArtist().getName() : "Unknown"));

        artistCombo.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
        statusCombo.setItems(FXCollections.observableArrayList(Arrays.asList(Artwork.Status.values())));
        refreshTable();

        artworkTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });
    }

    @FXML
    private void handleAddArtwork() {
        Artwork artwork = buildArtworkFromForm();
        if (artwork == null) {
            return;
        }
        try {
            artworkService.createArtwork(artwork);
            refreshTable();
            selectByTitle(artwork.getTitle());
        } catch (RuntimeException e) {
            showError("Failed to add artwork.", e);
        }
    }

    @FXML
    private void handleUpdateArtwork() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        Artwork artwork = buildArtworkFromForm();
        if (selected == null || artwork == null) {
            return;
        }
        if (!selected.getTitle().equals(artwork.getTitle())) {
            showInfo("Update requires the same artwork title as the selected row.");
            return;
        }
        try {
            artworkService.updateArtwork(artwork);
            refreshTable();
            selectByTitle(artwork.getTitle());
        } catch (RuntimeException e) {
            showError("Failed to update artwork.", e);
        }
    }

    @FXML
    private void handleDeleteArtwork() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        String targetTitle = selected != null ? selected.getTitle() : titleInput.getText();
        if (targetTitle == null || targetTitle.isBlank()) {
            showInfo("Select an artwork or enter a title to delete.");
            return;
        }
        try {
            artworkService.deleteArtwork(targetTitle.trim());
            refreshTable();
            handleClearForm();
        } catch (RuntimeException e) {
            showError("Failed to delete artwork.", e);
        }
    }

    @FXML
    private void handleClearForm() {
        titleInput.clear();
        typeInput.clear();
        priceInput.clear();
        yearInput.clear();
        artistCombo.setValue(null);
        statusCombo.setValue(null);
        artworkTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch() {
        String q = searchField.getText();
        if (q == null || q.isBlank()) {
            refreshTable();
            return;
        }
        String query = q.trim().toLowerCase();
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks().stream()
                .filter(a -> (a.getTitle() != null && a.getTitle().toLowerCase().contains(query))
                        || (a.getArtist() != null && a.getArtist().getName() != null 
                            && a.getArtist().getName().toLowerCase().contains(query))
                        || (a.getType() != null && a.getType().toLowerCase().contains(query)))
                .toList()));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        refreshTable();
    }

    private void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
    }

    private Artwork buildArtworkFromForm() {
        String title = titleInput.getText() != null ? titleInput.getText().trim() : "";
        if (title.isEmpty()) {
            showInfo("Title is required.");
            return null;
        }

        Artwork artwork = new Artwork();
        artwork.setTitle(title);
        artwork.setType(typeInput.getText() != null ? typeInput.getText().trim() : null);
        Artist selected = artistCombo.getValue();
        if (selected == null) {
            showInfo("Artist is required.");
            return null;
        }
        artwork.setArtist(selected);
        String priceText = priceInput.getText() != null ? priceInput.getText().trim() : "";
        if (!priceText.isEmpty()) {
            try {
                artwork.setPrice(Double.parseDouble(priceText));
            } catch (NumberFormatException e) {
                showInfo("Price must be a number.");
                return null;
            }
        }
        String yearText = yearInput.getText() != null ? yearInput.getText().trim() : "";
        if (!yearText.isEmpty()) {
            try {
                artwork.setCreationYear(Integer.parseInt(yearText));
            } catch (NumberFormatException e) {
                showInfo("Year must be a number.");
                return null;
            }
        }

        Artwork.Status status = statusCombo.getValue();
        if (status != null) {
            artwork.setStatus(status);
        } else {
            artwork.setStatus(Artwork.Status.FOR_SALE);
        }

        return artwork;
    }

    private void populateForm(Artwork artwork) {
        titleInput.setText(artwork.getTitle());
        typeInput.setText(artwork.getType());
        priceInput.setText(String.valueOf(artwork.getPrice()));
        yearInput.setText(artwork.getCreationYear() != null ? String.valueOf(artwork.getCreationYear()) : "");
        statusCombo.setValue(artwork.getStatus() != null ? artwork.getStatus() : Artwork.Status.FOR_SALE);
        if (artwork.getArtist() != null) {
            for (Artist a : artistCombo.getItems()) {
                if (a.getName() != null && a.getName().equalsIgnoreCase(artwork.getArtist().getName())) {
                    artistCombo.setValue(a);
                    break;
                }
            }
        } else {
            artistCombo.setValue(null);
        }
    }

    private void selectByTitle(String title) {
        if (title == null) {
            return;
        }
        for (Artwork artwork : artworkTable.getItems()) {
            if (title.equals(artwork.getTitle())) {
                artworkTable.getSelectionModel().select(artwork);
                break;
            }
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Artworks");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Artworks");
        alert.setHeaderText(message);
        alert.setContentText(exception.getMessage() != null ? exception.getMessage() : "An unexpected error occurred.");
        alert.showAndWait();
    }
}
