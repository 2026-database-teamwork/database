package database.assignment.domain.repair.repository;

import database.assignment.domain.repair.dto.CarRepairInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class RepairJdbcRepository implements RepairRepository{
    private final JdbcTemplate jdbcTemplate;

    public RepairJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<CarRepairInfo> findRepairHistory(Long carId) {
        String sql = "SELECT * from carrepairinfo where carId = ?";
        return jdbcTemplate.query(sql, RepairInfoRowMapper(), carId);
    }

    private RowMapper<CarRepairInfo> RepairInfoRowMapper(){
        return (rs, rowNum)->{
            return new CarRepairInfo(
                    rs.getLong("repair_id"),
                    rs.getLong("carId"),
                    rs.getLong("repair_shop_id"),
                    rs.getString("repair_detail"),
                    rs.getObject("repair_date", LocalDateTime.class),
                    rs.getInt("repair_cost")
            );
        };
    }
}
