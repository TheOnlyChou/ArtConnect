package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC implementation for ArtistDao.
 */
public class JdbcArtistDao implements ArtistDao {

    private static final String BASE_SELECT = """
            SELECT a.id_artist, a.name_artist, a.bio_artist, a.birthYear_artist,
                   a.contactEmail_artist, a.phone_artist, a.city_artist,
                   a.website_artist, a.socialMedia_artist, a.isActive,
                   ad.name_discipline
            FROM Artist a
            LEFT JOIN Artist_Discipline ad ON ad.id_artist = a.id_artist
            """;

    @Override
    public List<Artist> findAll() {
        String sql = BASE_SELECT + " ORDER BY a.name_artist";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            return mapArtists(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch artists from database.", e);
        }
    }

    @Override
    public void save(Artist artist) {
        String insertArtistSql = """
                INSERT INTO Artist(
                    id_artist, name_artist, birthYear_artist, contactEmail_artist,
                    bio_artist, phone_artist, city_artist, website_artist,
                    socialMedia_artist, isActive
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        String insertDisciplineSql = "INSERT INTO Artist_Discipline(id_artist, name_discipline) VALUES (?, ?)";

        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int artistId = nextArtistId(connection);
                try (PreparedStatement statement = connection.prepareStatement(insertArtistSql)) {
                    statement.setInt(1, artistId);
                    statement.setString(2, artist.getName());
                    setNullableInteger(statement, 3, artist.getBirthYear());
                    statement.setString(4, artist.getContactEmail());
                    statement.setString(5, artist.getBio());
                    statement.setString(6, artist.getPhone());
                    statement.setString(7, artist.getCity());
                    statement.setString(8, artist.getWebsite());
                    statement.setString(9, artist.getSocialMedia());
                    statement.setBoolean(10, artist.isActive());
                    statement.executeUpdate();
                }

                if (artist.getDisciplines() != null && !artist.getDisciplines().isEmpty()) {
                    try (PreparedStatement statement = connection.prepareStatement(insertDisciplineSql)) {
                        for (Discipline discipline : artist.getDisciplines()) {
                            statement.setInt(1, artistId);
                            statement.setString(2, discipline.getName());
                            statement.addBatch();
                        }
                        statement.executeBatch();
                    }
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save artist.", e);
        }
    }

    @Override
    public void update(Artist artist) {
        String updateArtistSql = """
                UPDATE Artist
                SET birthYear_artist = ?, contactEmail_artist = ?, bio_artist = ?,
                    phone_artist = ?, city_artist = ?, website_artist = ?,
                    socialMedia_artist = ?, isActive = ?
                WHERE name_artist = ?
                """;
        String deleteDisciplineSql = "DELETE FROM Artist_Discipline WHERE id_artist = ?";
        String insertDisciplineSql = "INSERT INTO Artist_Discipline(id_artist, name_discipline) VALUES (?, ?)";

        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int artistId = findArtistIdByName(connection, artist.getName());
                if (artistId == -1) {
                    connection.rollback();
                    return;
                }

                try (PreparedStatement statement = connection.prepareStatement(updateArtistSql)) {
                    setNullableInteger(statement, 1, artist.getBirthYear());
                    statement.setString(2, artist.getContactEmail());
                    statement.setString(3, artist.getBio());
                    statement.setString(4, artist.getPhone());
                    statement.setString(5, artist.getCity());
                    statement.setString(6, artist.getWebsite());
                    statement.setString(7, artist.getSocialMedia());
                    statement.setBoolean(8, artist.isActive());
                    statement.setString(9, artist.getName());
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(deleteDisciplineSql)) {
                    statement.setInt(1, artistId);
                    statement.executeUpdate();
                }

                if (artist.getDisciplines() != null && !artist.getDisciplines().isEmpty()) {
                    try (PreparedStatement statement = connection.prepareStatement(insertDisciplineSql)) {
                        for (Discipline discipline : artist.getDisciplines()) {
                            statement.setInt(1, artistId);
                            statement.setString(2, discipline.getName());
                            statement.addBatch();
                        }
                        statement.executeBatch();
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update artist.", e);
        }
    }

    @Override
    public void delete(String artistName) {
        String sql = "DELETE FROM Artist WHERE name_artist = ?";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, artistName);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete artist.", e);
        }
    }

    @Override
    public List<Artist> findByCity(String city) {
        String sql = BASE_SELECT + " WHERE a.city_artist = ? ORDER BY a.name_artist";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, city);
            try (ResultSet rs = statement.executeQuery()) {
                return mapArtists(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch artists by city.", e);
        }
    }

    private List<Artist> mapArtists(ResultSet rs) throws SQLException {
        Map<Integer, Artist> artistsById = new LinkedHashMap<>();
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

            String disciplineName = rs.getString("name_discipline");
            if (disciplineName != null) {
                artist.getDisciplines().add(new Discipline(disciplineName));
            }
        }
        return new ArrayList<>(artistsById.values());
    }

    private int nextArtistId(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COALESCE(MAX(id_artist), 0) + 1 FROM Artist");
                ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 1;
        }
    }

    private int findArtistIdByName(Connection connection, String artistName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id_artist FROM Artist WHERE name_artist = ?")) {
            statement.setString(1, artistName);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return -1;
            }
        }
    }

    private void setNullableInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }
}
