package com.project.artconnect.service.impl;

import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.BookingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryBookingService implements BookingService {
    private final List<Booking> bookings = new ArrayList<>();

    public InMemoryBookingService() {
        // keep empty by default
    }

    @Override
    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookings);
    }

    @Override
    public Optional<Booking> getBookingById(Long id) {
        return bookings.stream().filter(b -> b.getWorkshop() != null && b.getWorkshop().getId() != null && b.getWorkshop().getId().equals(id)).findFirst();
    }

    @Override
    public List<Booking> getBookingsByMember(CommunityMember member) {
        List<Booking> out = new ArrayList<>();
        for (Booking b : bookings) {
            if (b.getMember() != null && b.getMember().equals(member)) {
                out.add(b);
            }
        }
        return out;
    }

    @Override
    public List<Booking> getBookingsByWorkshop(Workshop workshop) {
        List<Booking> out = new ArrayList<>();
        for (Booking b : bookings) {
            if (b.getWorkshop() != null && b.getWorkshop().equals(workshop)) {
                out.add(b);
            }
        }
        return out;
    }

    @Override
    public void createBooking(Booking booking) {
        bookings.add(booking);
    }

    @Override
    public void updateBookingStatus(Booking booking, String paymentStatus) {
        booking.setPaymentStatus(paymentStatus);
    }

    @Override
    public void cancelBooking(Booking booking) {
        booking.setPaymentStatus("cancelled");
    }

    @Override
    public void deleteBooking(Booking booking) {
        bookings.remove(booking);
    }
}
