package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class CommunityController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> membershipFilter;
    @FXML
    private TextField nameInput;
    @FXML
    private TextField emailInput;
    @FXML
    private TextField cityInput;
    @FXML
    private TextField phoneInput;
    @FXML
    private TextField birthYearInput;
    @FXML
    private ComboBox<String> membershipInput;
    @FXML
    private TableView<CommunityMember> memberTable;
    @FXML
    private TableColumn<CommunityMember, String> nameColumn;
    @FXML
    private TableColumn<CommunityMember, String> emailColumn;
    @FXML
    private TableColumn<CommunityMember, String> cityColumn;
    @FXML
    private TableColumn<CommunityMember, String> membershipColumn;
    @FXML
    private TableColumn<CommunityMember, String> phoneColumn;
    @FXML
    private TableColumn<CommunityMember, Integer> birthYearColumn;

    private final CommunityService communityService = ServiceProvider.getCommunityService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        membershipColumn.setCellValueFactory(new PropertyValueFactory<>("membershipType"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        birthYearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));

        membershipInput.setItems(
                FXCollections.observableArrayList("standard", "premium", "student", "artist_supporter"));
        membershipFilter.setItems(
                FXCollections.observableArrayList("standard", "premium", "student", "artist_supporter"));

        memberTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String membership = membershipFilter.getValue();
        memberTable.setItems(FXCollections.observableArrayList(communityService.getAllMembers().stream()
                .filter(member -> query.isEmpty()
                        || (member.getName() != null && member.getName().toLowerCase().contains(query))
                        || (member.getEmail() != null && member.getEmail().toLowerCase().contains(query))
                        || (member.getCity() != null && member.getCity().toLowerCase().contains(query)))
                .filter(member -> membership == null
                        || (member.getMembershipType() != null
                                && membership.equalsIgnoreCase(member.getMembershipType())))
                .toList()));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        membershipFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleAddMember() {
        CommunityMember member = buildMemberFromForm();
        if (member == null) {
            return;
        }
        try {
            communityService.createMember(member);
            refreshTable();
            selectByEmail(member.getEmail());
        } catch (RuntimeException e) {
            showError("Failed to create member: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateMember() {
        CommunityMember selected = memberTable.getSelectionModel().getSelectedItem();
        CommunityMember member = buildMemberFromForm();
        if (selected == null || member == null) {
            showInfo("Select a member to update.");
            return;
        }
        if (!selected.getEmail().equalsIgnoreCase(member.getEmail())) {
            showInfo("Update requires the same email as the selected row.");
            return;
        }
        try {
            communityService.updateMember(member);
            refreshTable();
            selectByEmail(member.getEmail());
        } catch (RuntimeException e) {
            showError("Failed to update member: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteMember() {
        CommunityMember selected = memberTable.getSelectionModel().getSelectedItem();
        String targetEmail = selected != null ? selected.getEmail() : emailInput.getText();
        if (targetEmail == null || targetEmail.isBlank()) {
            showInfo("Select a member or enter an email to delete.");
            return;
        }
        try {
            communityService.deleteMember(targetEmail.trim());
            refreshTable();
            handleClearForm();
        } catch (RuntimeException e) {
            showError("Failed to delete member: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        nameInput.clear();
        emailInput.clear();
        cityInput.clear();
        phoneInput.clear();
        birthYearInput.clear();
        membershipInput.setValue(null);
        memberTable.getSelectionModel().clearSelection();
    }

    private void refreshTable() {
        memberTable.setItems(FXCollections.observableArrayList(communityService.getAllMembers()));
    }

    private CommunityMember buildMemberFromForm() {
        String name = nameInput.getText() != null ? nameInput.getText().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().trim() : "";
        if (name.isEmpty()) {
            showInfo("Name is required.");
            return null;
        }
        if (email.isEmpty()) {
            showInfo("Email is required.");
            return null;
        }
        String membership = membershipInput.getValue();
        if (membership == null) {
            showInfo("Membership type is required.");
            return null;
        }

        CommunityMember member = new CommunityMember();
        member.setName(name);
        member.setEmail(email);
        member.setCity(cityInput.getText() != null ? cityInput.getText().trim() : null);
        member.setPhone(phoneInput.getText() != null ? phoneInput.getText().trim() : null);
        member.setMembershipType(membership);

        String yearText = birthYearInput.getText() != null ? birthYearInput.getText().trim() : "";
        if (!yearText.isEmpty()) {
            try {
                member.setBirthYear(Integer.parseInt(yearText));
            } catch (NumberFormatException e) {
                showInfo("Birth Year must be a number.");
                return null;
            }
        }
        return member;
    }

    private void populateForm(CommunityMember member) {
        nameInput.setText(member.getName());
        emailInput.setText(member.getEmail());
        cityInput.setText(member.getCity());
        phoneInput.setText(member.getPhone());
        birthYearInput.setText(member.getBirthYear() != null ? String.valueOf(member.getBirthYear()) : "");
        membershipInput.setValue(member.getMembershipType());
    }

    private void selectByEmail(String email) {
        if (email == null) {
            return;
        }
        for (CommunityMember member : memberTable.getItems()) {
            if (email.equalsIgnoreCase(member.getEmail())) {
                memberTable.getSelectionModel().select(member);
                break;
            }
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Community");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Community");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
