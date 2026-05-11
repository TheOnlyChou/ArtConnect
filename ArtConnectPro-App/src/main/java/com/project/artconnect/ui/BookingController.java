package com.project.artconnect.ui;

import com.project.artconnect.model.Booking;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.service.BookingService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class BookingController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private ComboBox<CommunityMember> memberFilter;
    @FXML
    private ComboBox<Workshop> workshopInput;
    @FXML
    private ComboBox<CommunityMember> memberInput;
    @FXML
    private ComboBox<String> paymentStatusInput;
    @FXML
    private TableView<Booking> bookingTable;
    @FXML
    private TableColumn<Booking, String> memberColumn;
    @FXML
    private TableColumn<Booking, String> workshopColumn;
    @FXML
    private TableColumn<Booking, String> dateColumn;
    @FXML
    private TableColumn<Booking, String> statusColumn;

    private final BookingService bookingService = ServiceProvider.getBookingService();
    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private final CommunityService communityService = ServiceProvider.getCommunityService();
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        memberColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getMember() != null ? cellData.getValue().getMember().getName() : "Unknown"));
        
        workshopColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getWorkshop() != null ? cellData.getValue().getWorkshop().getTitle() : "Unknown"));
        
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getBookingDate() != null ? cellData.getValue().getBookingDate().format(DATE_TIME_FORMAT) : ""));
        
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getPaymentStatus() != null ? cellData.getValue().getPaymentStatus() : "pending"));

        workshopInput.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
        memberInput.setItems(FXCollections.observableArrayList(communityService.getAllMembers()));
        memberFilter.setItems(FXCollections.observableArrayList(communityService.getAllMembers()));
        
        paymentStatusInput.setItems(FXCollections.observableArrayList("pending", "paid", "cancelled"));
        statusFilter.setItems(FXCollections.observableArrayList("pending", "paid", "cancelled"));

        bookingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });
        
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String status = statusFilter.getValue();
        CommunityMember member = memberFilter.getValue();
        
        var filtered = bookingService.getAllBookings().stream()
            .filter(b -> query.isEmpty() || 
                   (b.getMember() != null && b.getMember().getName().toLowerCase().contains(query)) ||
                   (b.getWorkshop() != null && b.getWorkshop().getTitle().toLowerCase().contains(query)))
            .filter(b -> status == null || (b.getPaymentStatus() != null && b.getPaymentStatus().equalsIgnoreCase(status)))
            .filter(b -> member == null || (b.getMember() != null && b.getMember().equals(member)))
            .collect(Collectors.toList());
        
        bookingTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        statusFilter.setValue(null);
        memberFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleCreateBooking() {
        Workshop workshop = workshopInput.getValue();
        CommunityMember member = memberInput.getValue();
        
        if (workshop == null || member == null) {
            showInfo("Please select both a Workshop and a Member.");
            return;
        }
        
        Booking booking = new Booking(workshop, member);
        if (paymentStatusInput.getValue() != null) {
            booking.setPaymentStatus(paymentStatusInput.getValue());
        }
        
        try {
            bookingService.createBooking(booking);
            refreshTable();
            handleClearForm();
            showInfo("Booking created successfully.");
        } catch (RuntimeException e) {
            showError("Failed to create booking: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateStatus() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        String newStatus = paymentStatusInput.getValue();
        
        if (selected == null || newStatus == null) {
            showInfo("Select a booking and a payment status to update.");
            return;
        }
        
        try {
            bookingService.updateBookingStatus(selected, newStatus);
            refreshTable();
            showInfo("Booking status updated successfully.");
        } catch (RuntimeException e) {
            showError("Failed to update booking: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelBooking() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showInfo("Select a booking to cancel.");
            return;
        }
        
        try {
            bookingService.cancelBooking(selected);
            refreshTable();
            handleClearForm();
            showInfo("Booking cancelled successfully.");
        } catch (RuntimeException e) {
            showError("Failed to cancel booking: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        workshopInput.setValue(null);
        memberInput.setValue(null);
        paymentStatusInput.setValue(null);
        searchField.clear();
        bookingTable.getSelectionModel().clearSelection();
    }

    private void refreshTable() {
        bookingTable.setItems(FXCollections.observableArrayList(bookingService.getAllBookings()));
    }

    private void populateForm(Booking booking) {
        workshopInput.setValue(booking.getWorkshop());
        memberInput.setValue(booking.getMember());
        paymentStatusInput.setValue(booking.getPaymentStatus());
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bookings");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Bookings");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
