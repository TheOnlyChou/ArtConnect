package com.project.artconnect.util;

import com.project.artconnect.service.*;
import com.project.artconnect.service.impl.*;

/**
 * Service Provider to manage singleton instances of services and handle their
 * initialization.
 */
public class ServiceProvider {
    private static final ArtistService artistService = new JdbcArtistService();
    private static final ArtworkService artworkService = new JdbcArtworkService();
    private static final GalleryService galleryService = new JdbcGalleryService();
    private static final ExhibitionService exhibitionService = new JdbcExhibitionService();
    private static final WorkshopService workshopService = new JdbcWorkshopService();
    private static final CommunityService communityService = new JdbcCommunityService();
    private static final BookingService bookingService = new JdbcBookingService();

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
