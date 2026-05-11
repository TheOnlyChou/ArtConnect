package com.project.artconnect.service.impl;

import com.project.artconnect.dao.BookingDao;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.persistence.JdbcBookingDao;
import com.project.artconnect.service.BookingService;
import java.util.List;
import java.util.Optional;

public class JdbcBookingService implements BookingService {
    private final BookingDao bookingDao = new JdbcBookingDao();

    @Override
    public List<Booking> getAllBookings() {
        return bookingDao.findAll();
    }

    @Override
    public Optional<Booking> getBookingById(Long id) {
        return bookingDao.findById(id);
    }

    @Override
    public List<Booking> getBookingsByMember(CommunityMember member) {
        if (member == null || member.getId() == null) {
            throw new IllegalArgumentException("Member must not be null");
        }
        return bookingDao.findByMemberId(member.getId());
    }

    @Override
    public List<Booking> getBookingsByWorkshop(Workshop workshop) {
        if (workshop == null || workshop.getId() == null) {
            throw new IllegalArgumentException("Workshop must not be null");
        }
        return bookingDao.findByWorkshopId(workshop.getId());
    }

    @Override
    public void createBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking must not be null");
        }
        if (booking.getWorkshop() == null || booking.getMember() == null) {
            throw new IllegalArgumentException("Booking must have both workshop and member");
        }
        if (booking.getPaymentStatus() == null || booking.getPaymentStatus().isBlank()) {
            booking.setPaymentStatus("pending");
        } else {
            booking.setPaymentStatus(booking.getPaymentStatus().toLowerCase());
        }
        bookingDao.save(booking);
    }

    @Override
    public void updateBookingStatus(Booking booking, String paymentStatus) {
        if (booking == null || paymentStatus == null || paymentStatus.isBlank()) {
            throw new IllegalArgumentException("Booking and payment status must not be null");
        }
        if (booking.getWorkshop() == null || booking.getWorkshop().getId() == null
                || booking.getMember() == null || booking.getMember().getId() == null) {
            throw new IllegalArgumentException("Booking must include workshop and member IDs");
        }
        booking.setPaymentStatus(paymentStatus.toLowerCase());
        bookingDao.update(booking);
    }

    @Override
    public void cancelBooking(Booking booking) {
        updateBookingStatus(booking, "cancelled");
    }

    @Override
    public void deleteBooking(Booking booking) {
        if (booking == null || booking.getWorkshop() == null || booking.getWorkshop().getId() == null) {
            throw new IllegalArgumentException("Booking must include workshop ID");
        }
        bookingDao.delete(booking.getWorkshop().getId());
    }
}
