package database.assignment.domain.car.repository;

import database.assignment.domain.car.dto.Car;
import database.assignment.domain.car.dto.CarRegisterDto;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class CarJdbcRepository implements CarRepository{

    private final JdbcTemplate jdbcTemplate;

    public CarJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Car> findCarsByCompanyId(Long companyId) {
        String sql = "SELECT * FROM car WHERE companyId = ?";
        return jdbcTemplate.query(sql, carRowMapper(), companyId);
    }

    @Override
    public void createCar(CarRegisterDto car) {
        String sql = "INSERT INTO car (carName, carNumber, numberOfRider, carImageUrl, carDetail, carRentalCost) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                car.getCarName(),
                car.getCarNumber(),
                car.getNumberOfRider(),
                car.getCarImageUrl(),
                car.getCarDetail(),
                car.getCarRentalCost());
    }

    @Override
    public Optional<Car> findByCarId(Long carId) {
        String sql = "SELECT * from car WHERE carId = ?";
        return Optional.of(jdbcTemplate.queryForObject(sql, carRowMapper(), carId));
    }

    @Override
    public Optional<Car> findByCarNumber(String carNumber) {
        String sql = "SELECT * FROM car WHERE carNumber = ?";
        return Optional.of(jdbcTemplate.queryForObject(sql, carRowMapper(), carNumber));
    }

    private RowMapper<Car> carRowMapper(){
        return (rs, rowNum)->{
            return new Car(
                    rs.getLong("carId"),
                    rs.getLong("companyId"),
                    rs.getString("carName"),
                    rs.getString("carNumber"),
                    rs.getInt("numberOfRider"),
                    rs.getString("carImageUrl"),
                    rs.getString("carDetail"),
                    rs.getInt("carRentalCost")
            );
        };
    }
}
