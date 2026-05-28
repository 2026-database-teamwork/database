package database.assignment.domain.company.repository;

import database.assignment.domain.company.dto.Company;
import database.assignment.domain.company.dto.CompanyRegisterDto;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class CompanyJdbcRepository implements CompanyRepository{
    private final JdbcTemplate jdbcTemplate;

    public CompanyJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Company> findAllCompany() {
        String sql = "SELECT * FROM company";
        return jdbcTemplate.query(sql, companyRowMapper());
    }

    @Override
    public Optional<Company> findCompanyByName(String name) {
        String sql = "SELECT * FROM company WHERE companyName = ?";
        return Optional.of(jdbcTemplate.queryForObject(sql, companyRowMapper(), name));
    }

    @Override
    public Optional<Company> findCompanyById(Long id) {
        String sql = "SELECT * FROM company WHERE companyId = ?";
        return Optional.of(jdbcTemplate.queryForObject(sql, companyRowMapper(), id));
    }

    @Override
    public void createCompany(CompanyRegisterDto company) {
        String sql = "INSERT INTO company (companyName, companyAddress, companyPhone, representativeName, representativeEmail) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                company.getCompanyName(),
                company.getCompanyAddress(),
                company.getCompanyPhone(),
                company.getRepresentativeName(),
                company.getRepresentativeEmail()
        );
    }

    private RowMapper<Company> companyRowMapper(){
        return (rs, rowNum) -> {
            return new Company(
                    rs.getLong("companyId"),
                    rs.getString("companyName"),
                    rs.getString("companyAddress"),
                    rs.getString("companyPhone"),
                    rs.getString("representativeName"),
                    rs.getString("representativeEmail")
            );
        };
    }
}
