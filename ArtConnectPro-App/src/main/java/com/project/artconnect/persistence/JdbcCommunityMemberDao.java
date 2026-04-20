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

    private List<CommunityMember> mapMembers(ResultSet rs) throws SQLException {
        Map<Integer, CommunityMember> byId = new LinkedHashMap<>();
        while (rs.next()) {
            int memberId = rs.getInt("id_communityMember");
            CommunityMember member = byId.get(memberId);
            if (member == null) {
                member = new CommunityMember();
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
}
