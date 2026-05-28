package database.assignment.domain.car.repository;

import database.assignment.domain.car.dto.Car;
import database.assignment.domain.car.dto.CarRegisterDto;

import java.util.List;
import java.util.Optional;

public interface CarRepository {
    public List<Car> findCarsByCompanyId(Long companyId);
    public void createCar(CarRegisterDto carRegisterDto);
    public Optional<Car> findByCarId(Long carId);
    public Optional<Car> findByCarNumber(String carNumber);
}
