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
}
