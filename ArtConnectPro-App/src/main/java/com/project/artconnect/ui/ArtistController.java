package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private TextField phoneInput;
    @FXML
    private TextField websiteInput;
    @FXML
    private TextField socialInput;
    @FXML
    private TextArea bioInput;
    @FXML
    private CheckBox activeInput;
    @FXML
    private TextField disciplinesInput;
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
    @FXML
    private TableColumn<Artist, String> phoneColumn;
    @FXML
    private TableColumn<Artist, String> websiteColumn;
    @FXML
    private TableColumn<Artist, String> disciplinesColumn;
    @FXML
    private TableColumn<Artist, String> activeColumn;
    @FXML
    private TableColumn<Artist, String> bioPreviewColumn;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        websiteColumn.setCellValueFactory(new PropertyValueFactory<>("website"));
        disciplinesColumn.setCellValueFactory(cellData -> {
            Artist a = cellData.getValue();
            String joined = a.getDisciplines() == null || a.getDisciplines().isEmpty()
                    ? ""
                    : String.join(", ", a.getDisciplines().stream().map(d -> d.getName()).toList());
            return new javafx.beans.property.ReadOnlyStringWrapper(joined);
        });
        activeColumn.setCellValueFactory(cellData -> {
            boolean active = cellData.getValue() != null && cellData.getValue().isActive();
            return new javafx.beans.property.ReadOnlyStringWrapper(active ? "Yes" : "No");
        });
        bioPreviewColumn.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyStringWrapper(
            preview(cellData.getValue() != null ? cellData.getValue().getBio() : null, 80)));

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
        try {
            artistService.createArtist(artist);
            refreshTable();
            selectByName(artist.getName());
        } catch (RuntimeException e) {
            showError("Failed to create artist: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateArtist() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        Artist artist = buildArtistFromForm();
        if (selected == null || artist == null) {
            showInfo("Select an artist to update.");
            return;
        }
        if (!selected.getName().equals(artist.getName())) {
            showInfo("Update requires the same artist name as the selected row.");
            return;
        }
        try {
            artistService.updateArtist(artist);
            refreshTable();
            selectByName(artist.getName());
        } catch (RuntimeException e) {
            showError("Failed to update artist: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteArtist() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        String targetName = selected != null ? selected.getName() : nameInput.getText();
        if (targetName == null || targetName.isBlank()) {
            showInfo("Select an artist or enter a name to delete.");
            return;
        }
        try {
            artistService.deleteArtist(targetName.trim());
            refreshTable();
            handleClearForm();
        } catch (RuntimeException e) {
            showError("Failed to delete artist: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        nameInput.clear();
        cityInput.clear();
        emailInput.clear();
        yearInput.clear();
        phoneInput.clear();
        websiteInput.clear();
        socialInput.clear();
        bioInput.clear();
        disciplinesInput.clear();
        activeInput.setSelected(true);
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
        artist.setPhone(phoneInput.getText() != null ? phoneInput.getText().trim() : null);
        artist.setWebsite(websiteInput.getText() != null ? websiteInput.getText().trim() : null);
        artist.setSocialMedia(socialInput.getText() != null ? socialInput.getText().trim() : null);
        artist.setBio(bioInput.getText() != null ? bioInput.getText().trim() : null);
        artist.setActive(activeInput.isSelected());

        List<Discipline> disciplines = parseDisciplines(disciplinesInput.getText());
        if (disciplines == null) {
            return null;
        }
        artist.setDisciplines(disciplines);

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
        phoneInput.setText(artist.getPhone());
        websiteInput.setText(artist.getWebsite());
        socialInput.setText(artist.getSocialMedia());
        bioInput.setText(artist.getBio());
        activeInput.setSelected(artist.isActive());
        disciplinesInput.setText(artist.getDisciplines() == null
                ? ""
                : artist.getDisciplines().stream().map(Discipline::getName).collect(Collectors.joining(", ")));
    }

    private List<Discipline> parseDisciplines(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        Map<String, Discipline> byLowerName = new LinkedHashMap<>();
        for (Discipline discipline : artistService.getAllDisciplines()) {
            if (discipline.getName() != null) {
                byLowerName.put(discipline.getName().toLowerCase(), discipline);
            }
        }

        List<String> unknown = new java.util.ArrayList<>();
        List<Discipline> selected = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(name -> {
                    Discipline discipline = byLowerName.get(name.toLowerCase());
                    if (discipline == null) {
                        unknown.add(name);
                    }
                    return discipline;
                })
                .filter(d -> d != null)
                .collect(Collectors.toMap(
                        d -> d.getName().toLowerCase(),
                        d -> d,
                        (left, right) -> left,
                        LinkedHashMap::new))
                .values()
                .stream()
                .toList();

        if (!unknown.isEmpty()) {
            showInfo("Unknown disciplines: " + String.join(", ", unknown)
                    + ". Disciplines must be existing reference values. Use the discipline filter or ask an administrator to add new disciplines.");
            return null;
        }
        return selected;
    }

    private String preview(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String trimmed = text.trim().replace('\n', ' ');
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength - 1) + "...";
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Artists");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}