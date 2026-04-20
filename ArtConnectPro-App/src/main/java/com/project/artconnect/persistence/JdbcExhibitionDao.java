package com.project.artconnect.persistence;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JdbcExhibitionDao implements ExhibitionDao {

    private static final String BASE_SELECT = """
            SELECT e.id_exhibition, e.title_exhibition, e.startDate_exhibition,
                   e.endDate_exhibition, e.description_exhibition,
                   e.curatorName_exhibition, e.theme_exhibition,
                   g.id_gallery, g.name_gallery, g.address_gallery, g.ownerName_gallery,
                   g.openingHours_gallery, g.contactPhone_gallery, g.rating_gallery,
                   g.website_gallery,
                   aw.id_artwork, aw.title_artwork, aw.creationYear_artwork,
                   aw.type_artwork, aw.medium_artwork, aw.dimensions_artwork,
                   aw.price_artwork, aw.status_artwork, aw.description_artwork,
                   a.id_artist, a.name_artist
            FROM Exhibition e
            JOIN Gallery g ON g.id_gallery = e.id_gallery
            LEFT JOIN Artwork_Exhibition ae ON ae.id_exhibition = e.id_exhibition
            LEFT JOIN Artwork aw ON aw.id_artwork = ae.id_artwork
            LEFT JOIN Artist a ON a.id_artist = aw.id_artist
            """;

    @Override
    public List<Exhibition> findAll() {
        String sql = BASE_SELECT + " ORDER BY e.startDate_exhibition, e.title_exhibition";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            return mapExhibitions(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch exhibitions.", e);
        }
    }

    @Override
    public void save(Exhibition exhibition) {
        String insertSql = """
                INSERT INTO Exhibition(
                    id_exhibition, title_exhibition, startDate_exhibition,
                    endDate_exhibition, description_exhibition,
                    curatorName_exhibition, theme_exhibition, id_gallery
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int exhibitionId = nextExhibitionId(connection);
                int galleryId = resolveGalleryId(connection, exhibition.getGallery());
                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    statement.setInt(1, exhibitionId);
                    statement.setString(2, exhibition.getTitle());
                    statement.setDate(3, java.sql.Date.valueOf(exhibition.getStartDate()));
                    statement.setDate(4, java.sql.Date.valueOf(exhibition.getEndDate()));
                    statement.setString(5, exhibition.getDescription());
                    statement.setString(6, exhibition.getCuratorName());
                    statement.setString(7, exhibition.getTheme());
                    statement.setInt(8, galleryId);
                    statement.executeUpdate();
                }

                saveArtworkLinks(connection, exhibitionId, exhibition.getArtworks());
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save exhibition.", e);
        }
    }

    @Override
    public void update(Exhibition exhibition) {
        String updateSql = """
                UPDATE Exhibition
                SET startDate_exhibition = ?, endDate_exhibition = ?,
                    description_exhibition = ?, curatorName_exhibition = ?,
                    theme_exhibition = ?, id_gallery = ?
                WHERE title_exhibition = ?
                """;
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int exhibitionId = findExhibitionIdByTitle(connection, exhibition.getTitle());
                if (exhibitionId == -1) {
                    connection.rollback();
                    return;
                }
                int galleryId = resolveGalleryId(connection, exhibition.getGallery());

                try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                    statement.setDate(1, java.sql.Date.valueOf(exhibition.getStartDate()));
                    statement.setDate(2, java.sql.Date.valueOf(exhibition.getEndDate()));
                    statement.setString(3, exhibition.getDescription());
                    statement.setString(4, exhibition.getCuratorName());
                    statement.setString(5, exhibition.getTheme());
                    statement.setInt(6, galleryId);
                    statement.setString(7, exhibition.getTitle());
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection
                        .prepareStatement("DELETE FROM Artwork_Exhibition WHERE id_exhibition = ?")) {
                    statement.setInt(1, exhibitionId);
                    statement.executeUpdate();
                }
                saveArtworkLinks(connection, exhibitionId, exhibition.getArtworks());

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update exhibition.", e);
        }
    }

    @Override
    public void delete(String title) {
        String sql = "DELETE FROM Exhibition WHERE title_exhibition = ?";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete exhibition.", e);
        }
    }

    private List<Exhibition> mapExhibitions(ResultSet rs) throws SQLException {
        Map<Integer, Gallery> galleriesById = new LinkedHashMap<>();
        Map<Integer, Exhibition> exhibitionsById = new LinkedHashMap<>();

        while (rs.next()) {
            int galleryId = rs.getInt("id_gallery");
            Gallery gallery = galleriesById.get(galleryId);
            if (gallery == null) {
                gallery = new Gallery();
                gallery.setName(rs.getString("name_gallery"));
                gallery.setAddress(rs.getString("address_gallery"));
                gallery.setOwnerName(rs.getString("ownerName_gallery"));
                gallery.setOpeningHours(rs.getString("openingHours_gallery"));
                gallery.setContactPhone(rs.getString("contactPhone_gallery"));
                gallery.setRating(rs.getDouble("rating_gallery"));
                gallery.setWebsite(rs.getString("website_gallery"));
                galleriesById.put(galleryId, gallery);
            }

            int exhibitionId = rs.getInt("id_exhibition");
            Exhibition exhibition = exhibitionsById.get(exhibitionId);
            if (exhibition == null) {
                exhibition = new Exhibition();
                exhibition.setTitle(rs.getString("title_exhibition"));
                java.sql.Date startDate = rs.getDate("startDate_exhibition");
                java.sql.Date endDate = rs.getDate("endDate_exhibition");
                exhibition.setStartDate(startDate != null ? startDate.toLocalDate() : null);
                exhibition.setEndDate(endDate != null ? endDate.toLocalDate() : null);
                exhibition.setDescription(rs.getString("description_exhibition"));
                exhibition.setCuratorName(rs.getString("curatorName_exhibition"));
                exhibition.setTheme(rs.getString("theme_exhibition"));
                exhibition.setGallery(gallery);
                exhibitionsById.put(exhibitionId, exhibition);
                gallery.addExhibition(exhibition);
            }

            if (rs.getObject("id_artwork") != null) {
                Artwork artwork = new Artwork();
                artwork.setTitle(rs.getString("title_artwork"));
                artwork.setCreationYear((Integer) rs.getObject("creationYear_artwork"));
                artwork.setType(rs.getString("type_artwork"));
                artwork.setMedium(rs.getString("medium_artwork"));
                artwork.setDimensions(rs.getString("dimensions_artwork"));
                artwork.setPrice(rs.getDouble("price_artwork"));
                String status = rs.getString("status_artwork");
                if (status != null) {
                    artwork.setStatus(Artwork.Status.valueOf(status.toUpperCase()));
                } else {
                    artwork.setStatus(Artwork.Status.FOR_SALE);
                }
                artwork.setDescription(rs.getString("description_artwork"));

                if (rs.getObject("id_artist") != null) {
                    Artist artist = new Artist();
                    artist.setName(rs.getString("name_artist"));
                    artwork.setArtist(artist);
                }
                exhibition.getArtworks().add(artwork);
            }
        }

        return new ArrayList<>(exhibitionsById.values());
    }

    private int nextExhibitionId(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT COALESCE(MAX(id_exhibition), 0) + 1 FROM Exhibition");
                ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 1;
        }
    }

    private int findExhibitionIdByTitle(Connection connection, String title) throws SQLException {
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT id_exhibition FROM Exhibition WHERE title_exhibition = ?")) {
            statement.setString(1, title);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return -1;
            }
        }
    }

    private int resolveGalleryId(Connection connection, Gallery gallery) throws SQLException {
        if (gallery == null || gallery.getName() == null) {
            throw new SQLException("Exhibition must reference an existing gallery.");
        }
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT id_gallery FROM Gallery WHERE name_gallery = ?")) {
            statement.setString(1, gallery.getName());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Gallery not found for exhibition: " + gallery.getName());
    }

    private void saveArtworkLinks(Connection connection, int exhibitionId, List<Artwork> artworks) throws SQLException {
        if (artworks == null || artworks.isEmpty()) {
            return;
        }

        String resolveArtworkSql = "SELECT id_artwork FROM Artwork WHERE title_artwork = ?";
        String insertLinkSql = "INSERT INTO Artwork_Exhibition(id_artwork, id_exhibition) VALUES (?, ?)";

        try (PreparedStatement resolve = connection.prepareStatement(resolveArtworkSql);
                PreparedStatement insert = connection.prepareStatement(insertLinkSql)) {
            for (Artwork artwork : artworks) {
                if (artwork == null || artwork.getTitle() == null || artwork.getTitle().isBlank()) {
                    continue;
                }
                resolve.setString(1, artwork.getTitle());
                try (ResultSet rs = resolve.executeQuery()) {
                    if (rs.next()) {
                        insert.setInt(1, rs.getInt(1));
                        insert.setInt(2, exhibitionId);
                        insert.addBatch();
                    }
                }
            }
            insert.executeBatch();
        }
    }
}
