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
}
