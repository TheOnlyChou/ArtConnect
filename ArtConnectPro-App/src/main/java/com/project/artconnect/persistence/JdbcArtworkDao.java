package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC implementation for ArtworkDao.
 */
public class JdbcArtworkDao implements ArtworkDao {

    private static final String BASE_SELECT = """
            SELECT aw.id_artwork, aw.title_artwork, aw.creationYear_artwork, aw.type_artwork,
                   aw.medium_artwork, aw.dimensions_artwork, aw.price_artwork,
                   aw.status_artwork, aw.description_artwork,
                   a.id_artist, a.name_artist, a.bio_artist, a.birthYear_artist,
                   a.contactEmail_artist, a.phone_artist, a.city_artist,
                   a.website_artist, a.socialMedia_artist, a.isActive,
                   t.name_artworkTag
            FROM Artwork aw
            JOIN Artist a ON a.id_artist = aw.id_artist
            LEFT JOIN Artwork_ArtworkTag awt ON awt.id_artwork = aw.id_artwork
            LEFT JOIN ArtworkTag t ON t.name_artworkTag = awt.name_artworkTag
            """;

    @Override
    public List<Artwork> findAll() {
        String sql = BASE_SELECT + " ORDER BY aw.title_artwork";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            return mapArtworks(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch artworks from database.", e);
        }
    }

    @Override
    public void save(Artwork artwork) {
        String insertSql = """
                INSERT INTO Artwork(
                    id_artwork, title_artwork, creationYear_artwork, type_artwork,
                    medium_artwork, dimensions_artwork, price_artwork,
                    status_artwork, description_artwork, id_artist
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int artworkId = nextArtworkId(connection);
                int artistId = resolveArtistId(connection, artwork);
                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    statement.setInt(1, artworkId);
                    statement.setString(2, artwork.getTitle());
                    setNullableInteger(statement, 3, artwork.getCreationYear());
                    statement.setString(4, artwork.getType());
                    statement.setString(5, artwork.getMedium());
                    statement.setString(6, artwork.getDimensions());
                    statement.setDouble(7, artwork.getPrice());
                    statement.setString(8, artwork.getStatus() != null ? artwork.getStatus().name() : Artwork.Status.FOR_SALE.name());
                    statement.setString(9, artwork.getDescription());
                    statement.setInt(10, artistId);
                    statement.executeUpdate();
                }

                saveTags(connection, artworkId, artwork.getTags());
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save artwork.", e);
        }
    }

    @Override
    public void update(Artwork artwork) {
        String updateSql = """
                UPDATE Artwork
                SET creationYear_artwork = ?, type_artwork = ?, medium_artwork = ?,
                    dimensions_artwork = ?, price_artwork = ?, status_artwork = ?,
                    description_artwork = ?, id_artist = ?
                WHERE title_artwork = ?
                """;
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int artworkId = findArtworkIdByTitle(connection, artwork.getTitle());
                if (artworkId == -1) {
                    connection.rollback();
                    return;
                }
                int artistId = resolveArtistId(connection, artwork);

                try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                    setNullableInteger(statement, 1, artwork.getCreationYear());
                    statement.setString(2, artwork.getType());
                    statement.setString(3, artwork.getMedium());
                    statement.setString(4, artwork.getDimensions());
                    statement.setDouble(5, artwork.getPrice());
                    statement.setString(6, artwork.getStatus() != null ? artwork.getStatus().name() : Artwork.Status.FOR_SALE.name());
                    statement.setString(7, artwork.getDescription());
                    statement.setInt(8, artistId);
                    statement.setString(9, artwork.getTitle());
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection
                        .prepareStatement("DELETE FROM Artwork_ArtworkTag WHERE id_artwork = ?")) {
                    statement.setInt(1, artworkId);
                    statement.executeUpdate();
                }
                saveTags(connection, artworkId, artwork.getTags());

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update artwork.", e);
        }
    }

    @Override
    public void delete(String title) {
        String sql = "DELETE FROM Artwork WHERE title_artwork = ?";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete artwork.", e);
        }
    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        String sql = BASE_SELECT + " WHERE a.name_artist = ? ORDER BY aw.title_artwork";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, artistName);
            try (ResultSet rs = statement.executeQuery()) {
                return mapArtworks(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch artworks by artist name.", e);
        }
    }

    private List<Artwork> mapArtworks(ResultSet rs) throws SQLException {
        Map<Integer, Artist> artistsById = new LinkedHashMap<>();
        Map<Integer, Artwork> artworksById = new LinkedHashMap<>();

        while (rs.next()) {
            int artistId = rs.getInt("id_artist");
            Artist artist = artistsById.get(artistId);
            if (artist == null) {
                artist = new Artist();
                artist.setName(rs.getString("name_artist"));
                artist.setBio(rs.getString("bio_artist"));
                artist.setBirthYear((Integer) rs.getObject("birthYear_artist"));
                artist.setContactEmail(rs.getString("contactEmail_artist"));
                artist.setPhone(rs.getString("phone_artist"));
                artist.setCity(rs.getString("city_artist"));
                artist.setWebsite(rs.getString("website_artist"));
                artist.setSocialMedia(rs.getString("socialMedia_artist"));
                artist.setActive(rs.getBoolean("isActive"));
                artistsById.put(artistId, artist);
            }

            int artworkId = rs.getInt("id_artwork");
            Artwork artwork = artworksById.get(artworkId);
            if (artwork == null) {
                artwork = new Artwork();
                artwork.setTitle(rs.getString("title_artwork"));
                artwork.setCreationYear((Integer) rs.getObject("creationYear_artwork"));
                artwork.setType(rs.getString("type_artwork"));
                artwork.setMedium(rs.getString("medium_artwork"));
                artwork.setDimensions(rs.getString("dimensions_artwork"));
                artwork.setPrice(rs.getDouble("price_artwork"));
                String status = rs.getString("status_artwork");
                artwork.setStatus(status != null ? Artwork.Status.valueOf(status.toUpperCase()) : Artwork.Status.FOR_SALE);
                artwork.setDescription(rs.getString("description_artwork"));
                artwork.setArtist(artist);
                artworksById.put(artworkId, artwork);
                artist.addArtwork(artwork);
            }

            String tagName = rs.getString("name_artworkTag");
            if (tagName != null) {
                artwork.getTags().add(new ArtworkTag(tagName));
            }
        }
        return new ArrayList<>(artworksById.values());
    }

    private int nextArtworkId(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT COALESCE(MAX(id_artwork), 0) + 1 FROM Artwork");
                ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 1;
        }
    }

    private int findArtworkIdByTitle(Connection connection, String title) throws SQLException {
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT id_artwork FROM Artwork WHERE title_artwork = ?")) {
            statement.setString(1, title);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return -1;
            }
        }
    }

    private int resolveArtistId(Connection connection, Artwork artwork) throws SQLException {
        if (artwork == null || artwork.getArtist() == null || artwork.getArtist().getName() == null) {
            throw new SQLException("Artwork must reference an existing artist.");
        }

        try (PreparedStatement statement = connection
                .prepareStatement("SELECT id_artist FROM Artist WHERE name_artist = ?")) {
            statement.setString(1, artwork.getArtist().getName());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Artist not found for artwork: " + artwork.getArtist().getName());
    }

    private void saveTags(Connection connection, int artworkId, List<ArtworkTag> tags) throws SQLException {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        String upsertTagSql = """
                INSERT INTO ArtworkTag(name_artworkTag)
                VALUES (?)
                ON DUPLICATE KEY UPDATE name_artworkTag = VALUES(name_artworkTag)
                """;
        String insertTagLinkSql = "INSERT INTO Artwork_ArtworkTag(id_artwork, name_artworkTag) VALUES (?, ?)";

        try (PreparedStatement upsertTag = connection.prepareStatement(upsertTagSql);
                PreparedStatement insertTagLink = connection.prepareStatement(insertTagLinkSql)) {
            for (ArtworkTag tag : tags) {
                if (tag == null || tag.getName() == null || tag.getName().isBlank()) {
                    continue;
                }
                upsertTag.setString(1, tag.getName());
                upsertTag.addBatch();

                insertTagLink.setInt(1, artworkId);
                insertTagLink.setString(2, tag.getName());
                insertTagLink.addBatch();
            }
            upsertTag.executeBatch();
            insertTagLink.executeBatch();
        }
    }

    private void setNullableInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }
}
