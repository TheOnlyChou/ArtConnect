package com.project.artconnect.service.impl;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Review;
import com.project.artconnect.persistence.JdbcCommunityMemberDao;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JdbcCommunityService implements CommunityService {

    private final CommunityMemberDao memberDao;

    public JdbcCommunityService() {
        this(new JdbcCommunityMemberDao());
    }

    public JdbcCommunityService(CommunityMemberDao memberDao) {
        this.memberDao = memberDao;
    }

    @Override
    public List<CommunityMember> getAllMembers() {
        return memberDao.findAll();
    }

    @Override
    public Optional<CommunityMember> getMemberByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return memberDao.findAll().stream()
                .filter(member -> name.equalsIgnoreCase(member.getName()))
                .findFirst();
    }

    @Override
    public Optional<CommunityMember> getMemberByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return memberDao.findByEmail(email);
    }

    @Override
    public void createMember(CommunityMember member) {
        memberDao.save(member);
    }

    @Override
    public void updateMember(CommunityMember member) {
        memberDao.update(member);
    }

    @Override
    public void deleteMember(String email) {
        memberDao.delete(email);
    }

    @Override
    public List<Review> getReviewsByMember(CommunityMember member) {
        if (member == null) {
            return Collections.emptyList();
        }

        String sql = """
                SELECT r.rating_review, r.comment_review, r.reviewDate_review,
                       aw.title_artwork, aw.creationYear_artwork, aw.type_artwork,
                       aw.medium_artwork, aw.dimensions_artwork, aw.price_artwork,
                       aw.status_artwork, aw.description_artwork,
                       a.name_artist
                FROM Review r
                JOIN Artwork aw ON aw.id_artwork = r.id_artwork
                JOIN Artist a ON a.id_artist = aw.id_artist
                WHERE r.id_communityMember = ?
                ORDER BY r.reviewDate_review DESC
                """;

        try (Connection connection = ConnectionManager.getConnection()) {
            Integer memberId = resolveMemberId(connection, member);
            if (memberId == null) {
                return Collections.emptyList();
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, memberId);
                try (ResultSet rs = statement.executeQuery()) {
                    List<Review> reviews = new ArrayList<>();
                    while (rs.next()) {
                        Artist artist = new Artist();
                        artist.setName(rs.getString("name_artist"));

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
                        artwork.setArtist(artist);

                        Review review = new Review();
                        review.setReviewer(member);
                        review.setArtwork(artwork);
                        review.setRating(rs.getInt("rating_review"));
                        review.setComment(rs.getString("comment_review"));
                        java.sql.Date reviewDate = rs.getDate("reviewDate_review");
                        review.setReviewDate(reviewDate != null ? reviewDate.toLocalDate() : null);
                        reviews.add(review);
                    }
                    return reviews;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch reviews for member.", e);
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
