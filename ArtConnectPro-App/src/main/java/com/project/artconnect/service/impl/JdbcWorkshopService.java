package com.project.artconnect.service.impl;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.persistence.JdbcWorkshopDao;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JdbcWorkshopService implements WorkshopService {

    private final WorkshopDao workshopDao;

    public JdbcWorkshopService() {
        this(new JdbcWorkshopDao());
    }

    public JdbcWorkshopService(WorkshopDao workshopDao) {
        this.workshopDao = workshopDao;
    }

    @Override
    public List<Workshop> getAllWorkshops() {
        return workshopDao.findAll();
    }

    @Override
    public Optional<Workshop> getWorkshopByTitle(String title) {
        if (title == null) {
            return Optional.empty();
        }
        return workshopDao.findAll().stream()
                .filter(workshop -> title.equalsIgnoreCase(workshop.getTitle()))
                .findFirst();
    }

    @Override
    public void createWorkshop(Workshop workshop) {
        workshopDao.save(workshop);
    }

    @Override
    public void updateWorkshop(Workshop workshop) {
        workshopDao.update(workshop);
    }

    @Override
    public void deleteWorkshop(String title) {
        workshopDao.delete(title);
    }

    @Override
    public void bookWorkshop(Workshop workshop, CommunityMember member) {
        if (workshop == null || workshop.getTitle() == null || member == null) {
            return;
        }

        String insertSql = """
                INSERT INTO Booking(id_workshop, id_communityMember, paymentStatus_booking, bookingDate_booking)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE paymentStatus_booking = VALUES(paymentStatus_booking),
                                        bookingDate_booking = VALUES(bookingDate_booking)
                """;

        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Integer workshopId = resolveWorkshopId(connection, workshop.getTitle());
                Integer memberId = resolveMemberId(connection, member);
                if (workshopId == null || memberId == null) {
                    connection.rollback();
                    return;
                }

                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    statement.setInt(1, workshopId);
                    statement.setInt(2, memberId);
                    statement.setString(3, "pending");
                    statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create workshop booking.", e);
        }
    }

    @Override
    public List<Booking> getBookingsByMember(CommunityMember member) {
        if (member == null) {
            return Collections.emptyList();
        }

        String sql = """
                SELECT b.paymentStatus_booking, b.bookingDate_booking,
                       w.title_workshop, w.date_workshop, w.durationMinutes_workshop,
                       w.maxParticipants_workshop, w.price_workshop, w.location_workshop,
                       w.description_workshop, w.level_workshop,
                       a.name_artist
                FROM Booking b
                JOIN Workshop w ON w.id_workshop = b.id_workshop
                JOIN Artist a ON a.id_artist = w.id_artist
                WHERE b.id_communityMember = ?
                ORDER BY b.bookingDate_booking DESC
                """;

        try (Connection connection = ConnectionManager.getConnection()) {
            Integer memberId = resolveMemberId(connection, member);
            if (memberId == null) {
                return Collections.emptyList();
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, memberId);
                try (ResultSet rs = statement.executeQuery()) {
                    List<Booking> bookings = new ArrayList<>();
                    while (rs.next()) {
                        Workshop workshop = new Workshop();
                        workshop.setTitle(rs.getString("title_workshop"));
                        Timestamp workshopDate = rs.getTimestamp("date_workshop");
                        workshop.setDate(workshopDate != null ? workshopDate.toLocalDateTime() : null);
                        workshop.setDurationMinutes(rs.getInt("durationMinutes_workshop"));
                        workshop.setMaxParticipants(rs.getInt("maxParticipants_workshop"));
                        workshop.setPrice(rs.getDouble("price_workshop"));
                        workshop.setLocation(rs.getString("location_workshop"));
                        workshop.setDescription(rs.getString("description_workshop"));
                        workshop.setLevel(rs.getString("level_workshop"));

                        Artist instructor = new Artist();
                        instructor.setName(rs.getString("name_artist"));
                        workshop.setInstructor(instructor);

                        Booking booking = new Booking();
                        booking.setWorkshop(workshop);
                        booking.setMember(member);
                        Timestamp bookingDate = rs.getTimestamp("bookingDate_booking");
                        booking.setBookingDate(bookingDate != null ? bookingDate.toLocalDateTime() : null);
                        String status = rs.getString("paymentStatus_booking");
                        booking.setPaymentStatus(status != null ? status.toUpperCase() : "PENDING");
                        bookings.add(booking);
                    }
                    return bookings;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch member bookings.", e);
        }
    }

    private Integer resolveWorkshopId(Connection connection, String workshopTitle) throws SQLException {
        String sql = "SELECT id_workshop FROM Workshop WHERE title_workshop = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, workshopTitle);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return null;
            }
        }
    }

    private Integer resolveMemberId(Connection connection, CommunityMember member) throws SQLException {
        if (member.getEmail() != null && !member.getEmail().isBlank()) {
            String byEmailSql = "SELECT id_communityMember FROM CommunityMember WHERE email_communityMember = ?";
            try (PreparedStatement statement = connection.prepareStatement(byEmailSql)) {
                statement.setString(1, member.getEmail());
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }

        if (member.getName() != null && !member.getName().isBlank()) {
            String byNameSql = "SELECT id_communityMember FROM CommunityMember WHERE name_communityMember = ?";
            try (PreparedStatement statement = connection.prepareStatement(byNameSql)) {
                statement.setString(1, member.getName());
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return null;
    }
}
