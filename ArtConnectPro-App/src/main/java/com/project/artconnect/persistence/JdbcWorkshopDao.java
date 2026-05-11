package com.project.artconnect.persistence;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcWorkshopDao implements WorkshopDao {

    private static final String BASE_SELECT = """
            SELECT w.id_workshop, w.title_workshop, w.date_workshop, w.durationMinutes_workshop,
                   w.maxParticipants_workshop, w.price_workshop, w.location_workshop,
                   w.description_workshop, w.level_workshop,
                   a.name_artist, a.bio_artist, a.birthYear_artist, a.contactEmail_artist,
                   a.phone_artist, a.city_artist, a.website_artist, a.socialMedia_artist, a.isActive
            FROM Workshop w
            JOIN Artist a ON a.id_artist = w.id_artist
            """;

    @Override
    public Optional<Workshop> findById(Long id) {
        String sql = BASE_SELECT + " WHERE w.id_workshop = ?";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapWorkshop(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch workshop by id.", e);
        }
    }

    @Override
    public Optional<Workshop> findByTitle(String title) {
        String sql = BASE_SELECT + " WHERE w.title_workshop = ?";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapWorkshop(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch workshop by title.", e);
        }
    }

    @Override
    public List<Workshop> findAll() {
        String sql = BASE_SELECT + " ORDER BY w.date_workshop";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            List<Workshop> workshops = new ArrayList<>();
            while (rs.next()) {
                workshops.add(mapWorkshop(rs));
            }
            return workshops;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch workshops.", e);
        }
    }

    @Override
    public void save(Workshop workshop) {
        String insertSql = """
                INSERT INTO Workshop(
                    id_workshop, title_workshop, date_workshop, durationMinutes_workshop,
                    maxParticipants_workshop, price_workshop, location_workshop,
                    description_workshop, level_workshop, id_artist
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(insertSql)) {
            int nextId = nextWorkshopId(connection);
            int artistId = resolveArtistId(connection, workshop.getInstructor());

            statement.setInt(1, nextId);
            statement.setString(2, workshop.getTitle());
            statement.setTimestamp(3,
                    workshop.getDate() != null ? java.sql.Timestamp.valueOf(workshop.getDate()) : null);
            statement.setInt(4, workshop.getDurationMinutes());
            statement.setInt(5, workshop.getMaxParticipants());
            statement.setDouble(6, workshop.getPrice());
            statement.setString(7, workshop.getLocation());
            statement.setString(8, workshop.getDescription());
            statement.setString(9, workshop.getLevel());
            statement.setInt(10, artistId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save workshop.", e);
        }
    }

    @Override
    public void update(Workshop workshop) {
        String updateSql = """
                UPDATE Workshop
                SET date_workshop = ?, durationMinutes_workshop = ?, maxParticipants_workshop = ?,
                    price_workshop = ?, location_workshop = ?, description_workshop = ?,
                    level_workshop = ?, id_artist = ?
                WHERE title_workshop = ?
                """;
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(updateSql)) {
            int artistId = resolveArtistId(connection, workshop.getInstructor());

            statement.setTimestamp(1,
                    workshop.getDate() != null ? java.sql.Timestamp.valueOf(workshop.getDate()) : null);
            statement.setInt(2, workshop.getDurationMinutes());
            statement.setInt(3, workshop.getMaxParticipants());
            statement.setDouble(4, workshop.getPrice());
            statement.setString(5, workshop.getLocation());
            statement.setString(6, workshop.getDescription());
            statement.setString(7, workshop.getLevel());
            statement.setInt(8, artistId);
            statement.setString(9, workshop.getTitle());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update workshop.", e);
        }
    }

    @Override
    public void delete(String title) {
        String sql = "DELETE FROM Workshop WHERE title_workshop = ?";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete workshop.", e);
        }
    }

    private Workshop mapWorkshop(ResultSet rs) throws SQLException {
        Artist instructor = new Artist();
        instructor.setName(rs.getString("name_artist"));
        instructor.setBio(rs.getString("bio_artist"));
        instructor.setBirthYear((Integer) rs.getObject("birthYear_artist"));
        instructor.setContactEmail(rs.getString("contactEmail_artist"));
        instructor.setPhone(rs.getString("phone_artist"));
        instructor.setCity(rs.getString("city_artist"));
        instructor.setWebsite(rs.getString("website_artist"));
        instructor.setSocialMedia(rs.getString("socialMedia_artist"));
        instructor.setActive(rs.getBoolean("isActive"));

        Workshop workshop = new Workshop();
        workshop.setId(rs.getLong("id_workshop"));
        workshop.setTitle(rs.getString("title_workshop"));
        java.sql.Timestamp date = rs.getTimestamp("date_workshop");
        workshop.setDate(date != null ? date.toLocalDateTime() : null);
        workshop.setDurationMinutes(rs.getInt("durationMinutes_workshop"));
        workshop.setMaxParticipants(rs.getInt("maxParticipants_workshop"));
        workshop.setPrice(rs.getDouble("price_workshop"));
        workshop.setLocation(rs.getString("location_workshop"));
        workshop.setDescription(rs.getString("description_workshop"));
        workshop.setLevel(rs.getString("level_workshop"));
        workshop.setInstructor(instructor);
        return workshop;
    }

    private int nextWorkshopId(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT COALESCE(MAX(id_workshop), 0) + 1 FROM Workshop");
                ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 1;
        }
    }

    private int resolveArtistId(Connection connection, Artist artist) throws SQLException {
        if (artist == null || artist.getName() == null) {
            throw new SQLException("Workshop must reference an existing artist.");
        }
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT id_artist FROM Artist WHERE name_artist = ?")) {
            statement.setString(1, artist.getName());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Artist not found for workshop: " + artist.getName());
    }
}
