package database.assignment.domain.car.controller;

import database.assignment.domain.car.dto.CarRegisterDto;
import database.assignment.domain.car.service.CarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/car")
public class CarController {
    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addCar(@RequestBody CarRegisterDto carRegisterDto){
        carService.addCar(carRegisterDto);
        return ResponseEntity.ok("생성성공");
    }
}
