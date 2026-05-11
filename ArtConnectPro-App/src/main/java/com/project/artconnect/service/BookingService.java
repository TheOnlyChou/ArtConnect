package com.project.artconnect.service;

import com.project.artconnect.model.Booking;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.model.CommunityMember;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    List<Booking> getAllBookings();

    Optional<Booking> getBookingById(Long id);

    List<Booking> getBookingsByMember(CommunityMember member);

    List<Booking> getBookingsByWorkshop(Workshop workshop);

    void createBooking(Booking booking);

    void updateBookingStatus(Booking booking, String paymentStatus);

    void cancelBooking(Booking booking);

    void deleteBooking(Booking booking);
}
