package com.project.artconnect.ui;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.ExhibitionService;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.service.BookingService;
import com.project.artconnect.util.ServiceProvider;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class DiscoverController {
    @FXML
    private FlowPane discoverPane;
    @FXML
    private Label summaryLabel;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();
    private final ExhibitionService exhibitionService = ServiceProvider.getExhibitionService();
    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private final BookingService bookingService = ServiceProvider.getBookingService();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        List<Gallery> galleries = galleryService.getAllGalleries();
        List<Exhibition> featuredExhibitions = new ArrayList<>(exhibitionService.getAllExhibitions());
        List<Workshop> upcomingWorkshops = workshopService.getAllWorkshops();
        List<Booking> recentBookings = bookingService.getAllBookings();

        // Compute shown vs total counts and present them explicitly (e.g. "3 shown / 12 total")
        int shownGalleries = Math.min(3, galleries.size());
        int totalGalleries = galleries.size();
        int shownExhibitions = Math.min(3, featuredExhibitions.size());
        int totalExhibitions = featuredExhibitions.size();
        int shownWorkshops = Math.min(3, upcomingWorkshops.size());
        int totalWorkshops = upcomingWorkshops.size();
        int shownBookings = Math.min(5, recentBookings.size());
        int totalBookings = recentBookings.size();

        summaryLabel.setText("Galleries: " + shownGalleries + " shown / " + totalGalleries + " total"
            + " | Exhibitions: " + shownExhibitions + " shown / " + totalExhibitions + " total"
            + " | Workshops: " + shownWorkshops + " shown / " + totalWorkshops + " total"
            + " | Bookings: " + shownBookings + " shown / " + totalBookings + " total");

        featuredExhibitions.stream().limit(3).forEach(this::addExhibitionCard);
        galleries.stream().limit(3).forEach(this::addGalleryCard);
        upcomingWorkshops.stream().limit(3).forEach(this::addWorkshopCard);

        // Add a distinct, read-only Bookings section
        if (!recentBookings.isEmpty()) {
            addBookingsSection(recentBookings);
        }
    }

    private void addBookingsSection(List<Booking> bookings) {
        VBox section = new VBox(6);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: #fff3e0; -fx-border-color: #ff9800; -fx-border-radius: 5; -fx-background-radius: 5;");
        section.setPrefWidth(520);
        section.getChildren().add(new Label("RECENT BOOKINGS") {{ setStyle("-fx-font-weight: bold;"); }});

        bookings.stream().limit(5).forEach(b -> {
            String when = b.getBookingDate() != null ? b.getBookingDate().format(DATE_TIME_FORMAT) : "Unknown";
            String workshop = b.getWorkshop() != null ? b.getWorkshop().getTitle() : "Unknown workshop";
            String member = b.getMember() != null ? b.getMember().getName() : "Unknown member";
            String status = b.getPaymentStatus() != null ? b.getPaymentStatus() : "pending";
            Label entry = new Label(when + " — " + workshop + " — " + member + " (" + status + ")");
            section.getChildren().add(entry);
        });

        discoverPane.getChildren().add(section);
    }

    private void addExhibitionCard(Exhibition e) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefWidth(250);
        card.getChildren().addAll(
                new Label("FEATURED EXHIBITION"),
                new Label(e.getTitle()) {
                    {
                        setStyle("-fx-font-weight: bold;");
                    }
                },
                new Label("Theme: " + e.getTheme()),
                new Label("Gallery: " + (e.getGallery() != null ? e.getGallery().getName() : "Unknown")),
                new Label("Dates: " + (e.getStartDate() != null ? e.getStartDate().format(DATE_FORMAT) : "?")
                        + " to " + (e.getEndDate() != null ? e.getEndDate().format(DATE_FORMAT) : "?")),
                new Label("Curator: " + (e.getCuratorName() != null ? e.getCuratorName() : "Unknown")),
                new Label("About: " + preview(e.getDescription(), 90)));
        discoverPane.getChildren().add(card);
    }

    private void addGalleryCard(Gallery g) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: #fce4ec; -fx-border-color: #e91e63; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefWidth(250);
        card.getChildren().addAll(
                new Label("GALLERY"),
                new Label(g.getName()) {
                    {
                        setStyle("-fx-font-weight: bold;");
                    }
                },
                new Label("Owner: " + (g.getOwnerName() != null ? g.getOwnerName() : "Unknown")),
                new Label("Address: " + (g.getAddress() != null ? g.getAddress() : "Not specified")),
                new Label("Hours: " + (g.getOpeningHours() != null ? g.getOpeningHours() : "Check website")),
                new Label("Rating: " + (g.getRating() > 0 ? String.format("%.1f", g.getRating()) : "N/A")),
                new Label("Website: " + (g.getWebsite() != null && !g.getWebsite().isEmpty() ? preview(g.getWebsite(), 30) : "Not available")));
        discoverPane.getChildren().add(card);
    }

    private void addWorkshopCard(Workshop w) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: #f1f8e9; -fx-border-color: #4caf50; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefWidth(250);
        card.getChildren().addAll(
                new Label("UPCOMING WORKSHOP"),
                new Label(w.getTitle()) {
                    {
                        setStyle("-fx-font-weight: bold;");
                    }
                },
                new Label("Instructor: " + (w.getInstructor() != null ? w.getInstructor().getName() : "Unknown")),
                new Label("When: " + (w.getDate() != null ? w.getDate().format(DATE_TIME_FORMAT) : "Unknown")),
                new Label("Level: " + (w.getLevel() != null ? w.getLevel() : "Unknown")),
                new Label("Location: " + (w.getLocation() != null ? w.getLocation() : "Unknown")),
                new Label("Price: $" + w.getPrice()),
                new Label("About: " + preview(w.getDescription(), 90)));
        discoverPane.getChildren().add(card);
    }

    private String preview(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String trimmed = text.trim().replace('\n', ' ');
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength - 1) + "...";
    }
}
