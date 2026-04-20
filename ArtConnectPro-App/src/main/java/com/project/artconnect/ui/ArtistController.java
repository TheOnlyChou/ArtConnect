package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ArtistController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Discipline> disciplineFilter;
    @FXML
    private TextField nameInput;
    @FXML
    private TextField cityInput;
    @FXML
    private TextField emailInput;
    @FXML
    private TextField yearInput;
    @FXML
    private TableView<Artist> artistTable;
    @FXML
    private TableColumn<Artist, String> nameColumn;
    @FXML
    private TableColumn<Artist, String> cityColumn;
    @FXML
    private TableColumn<Artist, String> emailColumn;
    @FXML
    private TableColumn<Artist, Integer> yearColumn;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));

        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        artistTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        Discipline d = disciplineFilter.getValue();
        String dName = (d != null) ? d.getName() : null;
        artistTable.setItems(FXCollections.observableArrayList(artistService.searchArtists(query, dName, null)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        disciplineFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleAddArtist() {
        Artist artist = buildArtistFromForm();
        if (artist == null) {
            return;
        }
        artistService.createArtist(artist);
        refreshTable();
        selectByName(artist.getName());
    }

    @FXML
    private void handleUpdateArtist() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        Artist artist = buildArtistFromForm();
        if (selected == null || artist == null) {
            return;
        }
        if (!selected.getName().equals(artist.getName())) {
            showInfo("Update requires the same artist name as the selected row.");
            return;
        }
        artistService.updateArtist(artist);
        refreshTable();
        selectByName(artist.getName());
    }

    @FXML
    private void handleDeleteArtist() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        String targetName = selected != null ? selected.getName() : nameInput.getText();
        if (targetName == null || targetName.isBlank()) {
            showInfo("Select an artist or enter a name to delete.");
            return;
        }
        artistService.deleteArtist(targetName.trim());
        refreshTable();
        handleClearForm();
    }

    @FXML
    private void handleClearForm() {
        nameInput.clear();
        cityInput.clear();
        emailInput.clear();
        yearInput.clear();
        artistTable.getSelectionModel().clearSelection();
    }

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }

    private Artist buildArtistFromForm() {
        String name = nameInput.getText() != null ? nameInput.getText().trim() : "";
        if (name.isEmpty()) {
            showInfo("Name is required.");
            return null;
        }

        Artist artist = new Artist();
        artist.setName(name);
        artist.setCity(cityInput.getText() != null ? cityInput.getText().trim() : null);
        artist.setContactEmail(emailInput.getText() != null ? emailInput.getText().trim() : null);
        artist.setActive(true);

        String yearText = yearInput.getText() != null ? yearInput.getText().trim() : "";
        if (!yearText.isEmpty()) {
            try {
                artist.setBirthYear(Integer.parseInt(yearText));
            } catch (NumberFormatException e) {
                showInfo("Birth Year must be a number.");
                return null;
            }
        }
        return artist;
    }

    private void populateForm(Artist artist) {
        nameInput.setText(artist.getName());
        cityInput.setText(artist.getCity());
        emailInput.setText(artist.getContactEmail());
        yearInput.setText(artist.getBirthYear() != null ? String.valueOf(artist.getBirthYear()) : "");
    }

    private void selectByName(String name) {
        if (name == null) {
            return;
        }
        for (Artist artist : artistTable.getItems()) {
            if (name.equals(artist.getName())) {
                artistTable.getSelectionModel().select(artist);
                break;
            }
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Artists");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
