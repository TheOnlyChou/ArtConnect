package com.project.artconnect.persistence;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcGalleryDao implements GalleryDao {

    @Override
    public Optional<Gallery> findById(Long id) {
        String sql = """
                SELECT id_gallery, name_gallery, address_gallery, ownerName_gallery,
                       openingHours_gallery, contactPhone_gallery, rating_gallery, website_gallery
                FROM Gallery
                WHERE id_gallery = ?
                """;
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapGallery(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch gallery by id.", e);
        }
    }

    @Override
    public Optional<Gallery> findByName(String name) {
        String sql = """
                SELECT id_gallery, name_gallery, address_gallery, ownerName_gallery,
                       openingHours_gallery, contactPhone_gallery, rating_gallery, website_gallery
                FROM Gallery
                WHERE name_gallery = ?
                """;
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapGallery(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch gallery by name.", e);
        }
    }

    @Override
    public List<Gallery> findAll() {
        String sql = """
                SELECT id_gallery, name_gallery, address_gallery, ownerName_gallery,
                       openingHours_gallery, contactPhone_gallery, rating_gallery, website_gallery
                FROM Gallery
                ORDER BY name_gallery
                """;
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            List<Gallery> galleries = new ArrayList<>();
            while (rs.next()) {
                galleries.add(mapGallery(rs));
            }
            return galleries;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch galleries.", e);
        }
    }

    @Override
    public void save(Gallery gallery) {
        String insertSql = """
                INSERT INTO Gallery(
                    id_gallery, name_gallery, address_gallery, ownerName_gallery,
                    openingHours_gallery, contactPhone_gallery, rating_gallery, website_gallery
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(insertSql)) {
            int nextId = nextGalleryId(connection);
            statement.setInt(1, nextId);
            statement.setString(2, gallery.getName());
            statement.setString(3, gallery.getAddress());
            statement.setString(4, gallery.getOwnerName());
            statement.setString(5, gallery.getOpeningHours());
            statement.setString(6, gallery.getContactPhone());
            statement.setDouble(7, gallery.getRating());
            statement.setString(8, gallery.getWebsite());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save gallery.", e);
        }
    }

    @Override
    public void update(Gallery gallery) {
        String updateSql = """
                UPDATE Gallery
                SET address_gallery = ?, ownerName_gallery = ?, openingHours_gallery = ?,
                    contactPhone_gallery = ?, rating_gallery = ?, website_gallery = ?
                WHERE name_gallery = ?
                """;
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(updateSql)) {
            statement.setString(1, gallery.getAddress());
            statement.setString(2, gallery.getOwnerName());
            statement.setString(3, gallery.getOpeningHours());
            statement.setString(4, gallery.getContactPhone());
            statement.setDouble(5, gallery.getRating());
            statement.setString(6, gallery.getWebsite());
            statement.setString(7, gallery.getName());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update gallery.", e);
        }
    }

    @Override
    public void delete(String name) {
        String sql = "DELETE FROM Gallery WHERE name_gallery = ?";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete gallery.", e);
        }
    }

    private Gallery mapGallery(ResultSet rs) throws SQLException {
        Gallery gallery = new Gallery();
        gallery.setName(rs.getString("name_gallery"));
        gallery.setAddress(rs.getString("address_gallery"));
        gallery.setOwnerName(rs.getString("ownerName_gallery"));
        gallery.setOpeningHours(rs.getString("openingHours_gallery"));
        gallery.setContactPhone(rs.getString("contactPhone_gallery"));
        gallery.setRating(rs.getDouble("rating_gallery"));
        gallery.setWebsite(rs.getString("website_gallery"));
        return gallery;
    }

    private int nextGalleryId(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT COALESCE(MAX(id_gallery), 0) + 1 FROM Gallery");
                ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 1;
        }
    }
}
