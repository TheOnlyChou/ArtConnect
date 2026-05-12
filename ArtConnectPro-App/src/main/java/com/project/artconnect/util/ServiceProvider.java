package com.project.artconnect.util;

import com.project.artconnect.service.*;
import com.project.artconnect.service.impl.*;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Service Provider to manage singleton instances of services and handle their
 * initialization.
 */
public class ServiceProvider {
    private static final ArtistService artistService;
    private static final ArtworkService artworkService;
    private static final GalleryService galleryService;
    private static final ExhibitionService exhibitionService;
    private static final WorkshopService workshopService;
    private static final CommunityService communityService;
    private static final BookingService bookingService;
    static {
        boolean dbAvailable = true;
        try (Connection ignored = ConnectionManager.getConnection()) {
            // DB reachable
        } catch (SQLException | IllegalStateException e) {
            dbAvailable = false;
        }

        if (dbAvailable) {
            artistService = new JdbcArtistService();
            artworkService = new JdbcArtworkService();
            galleryService = new JdbcGalleryService();
            exhibitionService = new JdbcExhibitionService();
            workshopService = new JdbcWorkshopService();
            communityService = new JdbcCommunityService();
            bookingService = new JdbcBookingService();
        } else {
            // Fallback to in-memory implementations to allow UI to run without DB
            artistService = new InMemoryArtistService();
            artworkService = new InMemoryArtworkService();
            galleryService = new InMemoryGalleryService();
            exhibitionService = new InMemoryExhibitionService();
            workshopService = new InMemoryWorkshopService();
            communityService = new InMemoryCommunityService();
            bookingService = new InMemoryBookingService();
        }
    }

    public static ArtistService getArtistService() {
        return artistService;
    }

    public static ArtworkService getArtworkService() {
        return artworkService;
    }

    public static GalleryService getGalleryService() {
        return galleryService;
    }

    public static ExhibitionService getExhibitionService() {
        return exhibitionService;
    }

    public static WorkshopService getWorkshopService() {
        return workshopService;
    }

    public static CommunityService getCommunityService() {
        return communityService;
    }

    public static BookingService getBookingService() {
        return bookingService;
    }
}
