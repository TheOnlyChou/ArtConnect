package com.project.artconnect.persistence;

import com.project.artconnect.dao.BookingDao;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.util.ConnectionManager;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class JdbcBookingDao implements BookingDao {

    private static final String BASE_SELECT = """
         SELECT b.id_workshop AS booking_workshop_id,
             b.id_communityMember AS booking_member_id,
             b.bookingDate_booking,
             b.paymentStatus_booking,
             w.id_workshop,
             w.title_workshop,
             w.date_workshop,
             w.level_workshop,
             w.location_workshop,
             w.price_workshop,
             w.description_workshop,
             w.id_artist,
             a.name_artist,
             cm.id_communityMember,
             cm.name_communityMember,
             cm.email_communityMember
            FROM Booking b
            JOIN Workshop w ON w.id_workshop = b.id_workshop
         JOIN Artist a ON a.id_artist = w.id_artist
            JOIN CommunityMember cm ON cm.id_communityMember = b.id_communityMember
            """;

    @Override
    public Optional<Booking> findById(Long id) {
        // Note: Booking uses composite key, this finds by workshop ID
        String sql = BASE_SELECT + " WHERE b.id_workshop = ? LIMIT 1";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                List<Booking> bookings = mapBookings(rs);
                return bookings.isEmpty() ? Optional.empty() : Optional.of(bookings.get(0));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch booking by id.", e);
        }
    }

    @Override
    public List<Booking> findAll() {
        String sql = BASE_SELECT + " ORDER BY b.bookingDate_booking DESC";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            return mapBookings(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch bookings.", e);
        }
    }

    @Override
    public List<Booking> findByMemberId(Long memberId) {
        String sql = BASE_SELECT + " WHERE b.id_communityMember = ? ORDER BY b.bookingDate_booking DESC";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, memberId);
            try (ResultSet rs = statement.executeQuery()) {
                return mapBookings(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch bookings by member.", e);
        }
    }

    @Override
    public List<Booking> findByWorkshopId(Long workshopId) {
        String sql = BASE_SELECT + " WHERE b.id_workshop = ? ORDER BY b.bookingDate_booking DESC";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, workshopId);
            try (ResultSet rs = statement.executeQuery()) {
                return mapBookings(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch bookings by workshop.", e);
        }
    }

    @Override
    public void save(Booking booking) {
        String insertSql = """
                INSERT INTO Booking(
                    id_workshop, id_communityMember,
                    bookingDate_booking, paymentStatus_booking
                ) VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSql)) {
            if (booking.getWorkshop() == null || booking.getWorkshop().getId() == null
                    || booking.getMember() == null || booking.getMember().getId() == null) {
                throw new RuntimeException("Booking requires workshop and member IDs.");
            }
            statement.setLong(1, booking.getWorkshop().getId());
            statement.setLong(2, booking.getMember().getId());
            statement.setTimestamp(3, Timestamp.valueOf(booking.getBookingDate() != null ? booking.getBookingDate() : LocalDateTime.now()));
            statement.setString(4, booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "pending");
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create booking.", e);
        }
    }

    @Override
    public void update(Booking booking) {
        String updateSql = """
                UPDATE Booking
                SET paymentStatus_booking = ?
                WHERE id_workshop = ? AND id_communityMember = ?
                """;
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSql)) {
            if (booking.getWorkshop() == null || booking.getWorkshop().getId() == null
                    || booking.getMember() == null || booking.getMember().getId() == null) {
                throw new RuntimeException("Booking requires workshop and member IDs for update.");
            }
            statement.setString(1, booking.getPaymentStatus());
            statement.setLong(2, booking.getWorkshop().getId());
            statement.setLong(3, booking.getMember().getId());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Booking not found for update.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update booking.", e);
        }
    }

    @Override
    public void delete(Long id) {
        // Delete by workshop ID (composite key)
        String deleteSql = "DELETE FROM Booking WHERE id_workshop = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(deleteSql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete booking.", e);
        }
    }

    private List<Booking> mapBookings(ResultSet rs) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        while (rs.next()) {
            Booking booking = new Booking();
            booking.setBookingDate(rs.getTimestamp("bookingDate_booking").toLocalDateTime());
            booking.setPaymentStatus(rs.getString("paymentStatus_booking"));

            Workshop workshop = new Workshop();
            workshop.setId(rs.getLong("id_workshop"));
            workshop.setTitle(rs.getString("title_workshop"));
            Timestamp workshopDate = rs.getTimestamp("date_workshop");
            workshop.setDate(workshopDate != null ? workshopDate.toLocalDateTime() : null);
            workshop.setLevel(rs.getString("level_workshop"));
            workshop.setLocation(rs.getString("location_workshop"));
            workshop.setPrice(rs.getDouble("price_workshop"));
            workshop.setDescription(rs.getString("description_workshop"));
            booking.setWorkshop(workshop);

            CommunityMember member = new CommunityMember();
            member.setId(rs.getLong("id_communityMember"));
            member.setName(rs.getString("name_communityMember"));
            member.setEmail(rs.getString("email_communityMember"));
            booking.setMember(member);

            bookings.add(booking);
        }
        return bookings;
    }
}
