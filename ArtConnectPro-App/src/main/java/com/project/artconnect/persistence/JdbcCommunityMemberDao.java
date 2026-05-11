package com.project.artconnect.persistence;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
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
import java.util.Optional;

public class JdbcCommunityMemberDao implements CommunityMemberDao {

    private static final String BASE_SELECT = """
            SELECT cm.id_communityMember, cm.name_communityMember, cm.email_communityMember,
                   cm.birthYear_communityMember, cm.phone_communityMember,
                   cm.city_communityMember, cm.membershipType_communityMember,
                   cmd.name_discipline
            FROM CommunityMember cm
            LEFT JOIN CommunityMember_Discipline cmd
                   ON cmd.id_communityMember = cm.id_communityMember
            """;

    @Override
    public Optional<CommunityMember> findById(Long id) {
        String sql = BASE_SELECT + " WHERE cm.id_communityMember = ? ORDER BY cm.name_communityMember";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                List<CommunityMember> members = mapMembers(rs);
                if (members.isEmpty()) {
                    return Optional.empty();
                }
                return Optional.of(members.get(0));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch member by id.", e);
        }
    }

    @Override
    public Optional<CommunityMember> findByEmail(String email) {
        String sql = BASE_SELECT + " WHERE cm.email_communityMember = ? ORDER BY cm.name_communityMember";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                List<CommunityMember> members = mapMembers(rs);
                if (members.isEmpty()) {
                    return Optional.empty();
                }
                return Optional.of(members.get(0));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch member by email.", e);
        }
    }

    @Override
    public List<CommunityMember> findAll() {
        String sql = BASE_SELECT + " ORDER BY cm.name_communityMember";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            return mapMembers(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch community members.", e);
        }
    }

    @Override
    public void save(CommunityMember member) {
        String insertSql = """
                INSERT INTO CommunityMember(
                    id_communityMember, name_communityMember, email_communityMember,
                    birthYear_communityMember, phone_communityMember,
                    city_communityMember, membershipType_communityMember
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(insertSql)) {
            int nextId = nextMemberId(connection);
            statement.setInt(1, nextId);
            statement.setString(2, member.getName());
            statement.setString(3, member.getEmail());
            if (member.getBirthYear() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, member.getBirthYear());
            }
            statement.setString(5, member.getPhone());
            statement.setString(6, member.getCity());
            statement.setString(7, member.getMembershipType());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save community member.", e);
        }
    }

    @Override
    public void update(CommunityMember member) {
        String updateSql = """
                UPDATE CommunityMember
                SET name_communityMember = ?, birthYear_communityMember = ?, phone_communityMember = ?,
                    city_communityMember = ?, membershipType_communityMember = ?
                WHERE email_communityMember = ?
                """;
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(updateSql)) {
            statement.setString(1, member.getName());
            if (member.getBirthYear() == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, member.getBirthYear());
            }
            statement.setString(3, member.getPhone());
            statement.setString(4, member.getCity());
            statement.setString(5, member.getMembershipType());
            statement.setString(6, member.getEmail());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update community member.", e);
        }
    }

    @Override
    public void delete(String email) {
        String sql = "DELETE FROM CommunityMember WHERE email_communityMember = ?";
        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete community member.", e);
        }
    }

    private List<CommunityMember> mapMembers(ResultSet rs) throws SQLException {
        Map<Integer, CommunityMember> byId = new LinkedHashMap<>();
        while (rs.next()) {
            int memberId = rs.getInt("id_communityMember");
            CommunityMember member = byId.get(memberId);
            if (member == null) {
                member = new CommunityMember();
                member.setId((long) memberId);
                member.setName(rs.getString("name_communityMember"));
                member.setEmail(rs.getString("email_communityMember"));
                member.setBirthYear((Integer) rs.getObject("birthYear_communityMember"));
                member.setPhone(rs.getString("phone_communityMember"));
                member.setCity(rs.getString("city_communityMember"));
                member.setMembershipType(rs.getString("membershipType_communityMember"));
                byId.put(memberId, member);
            }

            String disciplineName = rs.getString("name_discipline");
            if (disciplineName != null) {
                member.getFavoriteDisciplines().add(new Discipline(disciplineName));
            }
        }
        return new ArrayList<>(byId.values());
    }

    private int nextMemberId(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT COALESCE(MAX(id_communityMember), 0) + 1 FROM CommunityMember");
                ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 1;
        }
    }
}
