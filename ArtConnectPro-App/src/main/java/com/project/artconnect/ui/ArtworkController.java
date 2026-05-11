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
import java.util.stream.Collectors;

public class ArtworkController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Artwork.Status> statusFilter;
    @FXML
    private ComboBox<Artist> artistFilter;
    @FXML
    private TextField titleInput;
    @FXML
    private TextField creationYearInput;
    @FXML
    private TextField typeInput;
    @FXML
    private TextField mediumInput;
    @FXML
    private TextField dimensionsInput;
    @FXML
    private TextField priceInput;
    @FXML
    private ComboBox<Artwork.Status> statusInput;
    @FXML
    private ComboBox<Artist> artistInput;
    @FXML
    private TextField tagsInput;
    @FXML
    private TextArea descriptionInput;
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
    private TableColumn<Artwork, String> dimensionsColumn;
    @FXML
    private TableColumn<Artwork, String> descriptionPreviewColumn;
    @FXML
    private TableColumn<Artwork, String> artistColumn;
    @FXML
    private TableColumn<Artwork, String> tagsColumn;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("creationYear"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().toString() : ""));
        dimensionsColumn.setCellValueFactory(new PropertyValueFactory<>("dimensions"));
        descriptionPreviewColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
            preview(cellData.getValue() != null ? cellData.getValue().getDescription() : null, 70)));

        tagsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getTags() != null && !cellData.getValue().getTags().isEmpty()
                ? cellData.getValue().getTags().stream().map(t -> t.getName()).collect(Collectors.joining(", "))
                : ""));

        artistColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getArtist() != null ? cellData.getValue().getArtist().getName() : "Unknown"));

        artistFilter.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
        statusFilter.setItems(FXCollections.observableArrayList(Artwork.Status.values()));
        statusInput.setItems(FXCollections.observableArrayList(Artwork.Status.values()));
        artistInput.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));

        artworkTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        Artwork.Status status = statusFilter.getValue();
        Artist artist = artistFilter.getValue();
        
        var filtered = artworkService.getAllArtworks().stream()
            .filter(a -> query.isEmpty() || 
                   a.getTitle().toLowerCase().contains(query) || 
                   (a.getType() != null && a.getType().toLowerCase().contains(query)) ||
                   (a.getArtist() != null && a.getArtist().getName().toLowerCase().contains(query)))
            .filter(a -> status == null || a.getStatus() == status)
            .filter(a -> artist == null || (a.getArtist() != null && a.getArtist().equals(artist)))
            .collect(Collectors.toList());
        
        artworkTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        statusFilter.setValue(null);
        artistFilter.setValue(null);
        refreshTable();
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
            showError("Failed to create artwork: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateArtwork() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        Artwork artwork = buildArtworkFromForm();
        if (selected == null || artwork == null) {
            showInfo("Select an artwork to update.");
            return;
        }
        if (!selected.getTitle().equals(artwork.getTitle())) {
            showInfo("Update requires the same title as the selected row.");
            return;
        }
        try {
            artworkService.updateArtwork(artwork);
            refreshTable();
            selectByTitle(artwork.getTitle());
        } catch (RuntimeException e) {
            showError("Failed to update artwork: " + e.getMessage());
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
            showError("Failed to delete artwork: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        titleInput.clear();
        creationYearInput.clear();
        typeInput.clear();
        mediumInput.clear();
        dimensionsInput.clear();
        priceInput.clear();
        statusInput.setValue(null);
        artistInput.setValue(null);
        tagsInput.clear();
        descriptionInput.clear();
        artworkTable.getSelectionModel().clearSelection();
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

        Artist artist = artistInput.getValue();
        if (artist == null) {
            showInfo("Artist is required.");
            return null;
        }

        Artwork artwork = new Artwork();
        artwork.setTitle(title);
        artwork.setArtist(artist);
        artwork.setType(typeInput.getText() != null ? typeInput.getText().trim() : null);
        artwork.setMedium(mediumInput.getText() != null ? mediumInput.getText().trim() : null);
        artwork.setDimensions(dimensionsInput.getText() != null ? dimensionsInput.getText().trim() : null);
        artwork.setDescription(descriptionInput.getText() != null ? descriptionInput.getText().trim() : null);

        if (statusInput.getValue() != null) {
            artwork.setStatus(statusInput.getValue());
        }

        String priceText = priceInput.getText() != null ? priceInput.getText().trim() : "";
        if (!priceText.isEmpty()) {
            try {
                artwork.setPrice(Double.parseDouble(priceText));
            } catch (NumberFormatException e) {
                showInfo("Price must be a valid number.");
                return null;
            }
        } else {
            artwork.setPrice(0.0);
        }

        String yearText = creationYearInput.getText() != null ? creationYearInput.getText().trim() : "";
        if (!yearText.isEmpty()) {
            try {
                artwork.setCreationYear(Integer.parseInt(yearText));
            } catch (NumberFormatException e) {
                showInfo("Year must be a number.");
                return null;
            }
        }

        return artwork;
    }

    private void populateForm(Artwork artwork) {
        titleInput.setText(artwork.getTitle());
        typeInput.setText(artwork.getType());
        mediumInput.setText(artwork.getMedium());
        dimensionsInput.setText(artwork.getDimensions());
        priceInput.setText(artwork.getPrice() > 0 ? String.valueOf(artwork.getPrice()) : "");
        creationYearInput.setText(artwork.getCreationYear() != null ? String.valueOf(artwork.getCreationYear()) : "");
        statusInput.setValue(artwork.getStatus());
        artistInput.setValue(artwork.getArtist());
        tagsInput.setText(artwork.getTags() != null && !artwork.getTags().isEmpty()
            ? artwork.getTags().stream().map(t -> t.getName()).collect(Collectors.joining(", "))
            : "");
        descriptionInput.setText(artwork.getDescription());
    }

    private String preview(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String trimmed = text.trim().replace('\n', ' ');
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength - 1) + "...";
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Artworks");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
