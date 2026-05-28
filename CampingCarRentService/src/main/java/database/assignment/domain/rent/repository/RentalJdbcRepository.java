package database.assignment.domain.rent.repository;

import database.assignment.domain.rent.dto.Rental;
import database.assignment.domain.rent.dto.RentalRequestDto;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Primary
@Repository
public class RentalJdbcRepository implements RentalRepository{

    private final JdbcTemplate jdbcTemplate;

    public RentalJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Rental> findRentalHistory(String license) {
        String sql = "SELECT * FROM rent WHERE license = ?";
        return jdbcTemplate.query(sql, rentalRowMapper(), license);
    }

    @Override
    public List<Rental> findRentalHistoryByCarId(Long carId) {
        String sql = "SELECT * FROM rent WHERE carId = ?";
        return jdbcTemplate.query(sql, rentalRowMapper(), carId);
    }

    @Override
    public void createRentalHistory(RentalRequestDto rental, String license) {
        String sql = "INSERT INTO rent (carId, license, companyId, startDateTime, endDateTime, totalCost) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                rental.getCarId(),
                license,
                rental.getCompanyId(),
                rental.getStartDateTime(),
                rental.getEndDateTime(),
                rental.getTotalCost());
    }

    private RowMapper<Rental> rentalRowMapper(){
        return (rs, rowNum)->{
            return new Rental(
                    rs.getLong("rentalId"),
                    rs.getLong("carId"),
                    rs.getString("license"),
                    rs.getLong("companyId"),
                    rs.getObject("startDateTime", LocalDateTime.class),
                    rs.getObject("endDateTime", LocalDateTime.class),
                    rs.getInt("totalCost")
            );
        };
    }

}
