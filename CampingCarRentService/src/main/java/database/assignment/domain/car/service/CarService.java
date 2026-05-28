package database.assignment.domain.car.service;

import database.assignment.domain.car.dto.Car;
import database.assignment.domain.car.dto.CarRegisterDto;
import database.assignment.domain.car.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CarService {
    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public Optional<Car> findCarById(Long carId){
        return carRepository.findByCarId(carId);
    }

    public void addCar(CarRegisterDto carRegisterDto){
        carRepository.createCar(carRegisterDto);
    }
}
