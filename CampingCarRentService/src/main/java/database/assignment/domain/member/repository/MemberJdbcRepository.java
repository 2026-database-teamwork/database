package database.assignment.domain.member.repository;

import database.assignment.domain.member.dto.Member;
import database.assignment.domain.member.dto.MemberSignUpDto;
import database.assignment.domain.member.dto.Role;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class MemberJdbcRepository implements MemberRepository{
    private final JdbcTemplate jdbcTemplate;

    public MemberJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Member> findMember(String name) {
        String sql = "SELECT * FROM member WHERE name = ?";
        List<Member> results = jdbcTemplate.query(sql, memberRowMapper(), name);
        return results.stream().findFirst();
    }

    @Override
    public Optional<Member> findMemberByLicense(String license) {
        String sql = "SELECT * FROM member WHERE license = ?";
        return Optional.of(jdbcTemplate.queryForObject(sql, memberRowMapper(), license));
    }

    @Override
    public List<Member> findAllMember() {
        String sql = "SELECT * FROM member";
        return jdbcTemplate.query(sql, memberRowMapper());
    }

    @Override
    public void createMember(MemberSignUpDto member) {
        String sql = "INSERT INTO member (license, role, name, password, address, phone, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                member.getLicense(),
                Role.USER.name(),
                member.getName(),
                member.getPassword(),
                member.getAddress(),
                member.getPhone(),
                member.getEmail()
        );
    }

    private RowMapper<Member> memberRowMapper(){
        return (rs, rowNum) -> {
            String roleStr = rs.getString("role");

            Role roleEnum = Role.fromString(roleStr);
            return new Member(
                    rs.getString("license"),
                    roleEnum,
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getString("address"),
                    rs.getString("phone"),
                    rs.getString("email")
            );
        };
    }
}
