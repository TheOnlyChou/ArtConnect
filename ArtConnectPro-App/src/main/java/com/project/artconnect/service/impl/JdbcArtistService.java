package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.persistence.JdbcArtistDao;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JdbcArtistService implements ArtistService {

    private final ArtistDao artistDao;

    public JdbcArtistService() {
        this(new JdbcArtistDao());
    }

    public JdbcArtistService(ArtistDao artistDao) {
        this.artistDao = artistDao;
    }

    @Override
    public List<Artist> getAllArtists() {
        return artistDao.findAll();
    }

    @Override
    public Optional<Artist> getArtistByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return artistDao.findAll().stream()
                .filter(a -> name.equalsIgnoreCase(a.getName()))
                .findFirst();
    }

    @Override
    public void createArtist(Artist artist) {
        artistDao.save(artist);
    }

    @Override
    public void updateArtist(Artist artist) {
        artistDao.update(artist);
    }

    @Override
    public void deleteArtist(String name) {
        artistDao.delete(name);
    }

    @Override
    public List<Discipline> getAllDisciplines() {
        String sql = "SELECT name_discipline FROM Discipline ORDER BY name_discipline";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            List<Discipline> disciplines = new ArrayList<>();
            while (rs.next()) {
                disciplines.add(new Discipline(rs.getString("name_discipline")));
            }
            return disciplines;
        } catch (SQLException e) {
            List<Discipline> fallback = artistDao.findAll().stream()
                    .flatMap(artist -> artist.getDisciplines().stream())
                    .collect(Collectors.toMap(
                            Discipline::getName,
                            d -> d,
                            (left, right) -> left,
                            LinkedHashMap::new))
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(Discipline::getName))
                    .collect(Collectors.toList());
            return fallback;
        }
    }

    @Override
    public List<Artist> searchArtists(String query, String disciplineName, String city) {
        List<Artist> source = artistDao.findAll();
        String normalizedQuery = query == null ? null : query.trim().toLowerCase();
        String normalizedDiscipline = disciplineName == null ? null : disciplineName.trim().toLowerCase();

        return source.stream()
            .filter(a -> {
                if (normalizedQuery == null || normalizedQuery.isEmpty()) {
                    return true;
                }
                // name
                if (a.getName() != null && a.getName().toLowerCase().contains(normalizedQuery)) {
                    return true;
                }
                // birth year
                if (a.getBirthYear() != null && String.valueOf(a.getBirthYear()).contains(normalizedQuery)) {
                    return true;
                }
                // disciplines
                if (a.getDisciplines() != null && a.getDisciplines().stream()
                        .anyMatch(d -> d.getName() != null && d.getName().toLowerCase().contains(normalizedQuery))) {
                    return true;
                }
                // active/inactive keyword
                if ("active".equals(normalizedQuery) && a.isActive()) {
                    return true;
                }
                if ("inactive".equals(normalizedQuery) && !a.isActive()) {
                    return true;
                }
                return false;
            })
            .filter(a -> normalizedDiscipline == null || normalizedDiscipline.isEmpty()
                    || a.getDisciplines().stream()
                            .anyMatch(d -> d.getName() != null
                                    && d.getName().toLowerCase().equals(normalizedDiscipline)))
            .collect(Collectors.toList());
    }
}
