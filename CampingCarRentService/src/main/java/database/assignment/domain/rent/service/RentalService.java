package database.assignment.domain.rent.service;

import database.assignment.domain.car.repository.CarRepository;
import database.assignment.domain.car.service.CarService;
import database.assignment.domain.company.repository.CompanyRepository;
import database.assignment.domain.company.service.CompanyService;
import database.assignment.domain.rent.dto.Rental;
import database.assignment.domain.rent.dto.RentalHistoryResponseDto;
import database.assignment.domain.rent.dto.RentalRequestDto;
import database.assignment.domain.rent.event.RentCompletedEvent;
import database.assignment.domain.rent.repository.RentalRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentalService {
    private final RentalRepository rentalRepository;
    private final CarService carService;
    private final CompanyService companyService;
    private final ApplicationEventPublisher eventPublisher;

    public RentalService(RentalRepository rentalRepository, CarService carService,
                         CompanyService companyService, ApplicationEventPublisher eventPublisher) {
        this.rentalRepository = rentalRepository;
        this.carService = carService;
        this.companyService = companyService;
        this.eventPublisher = eventPublisher;
    }

    private List<RentalHistoryResponseDto> convertToHistoryDto(List<Rental> rentals){
        return rentals.stream().map(rental -> {
            String carName = carService.findCarById(rental.getCarId()).get().getCarName();
            String companyName = companyService.findCompanyById(rental.getCompanyId()).get().getCompanyName();
            return new RentalHistoryResponseDto(
                    carName,
                    rental.getLicense(),
                    companyName,
                    rental.getStartDateTime(),
                    rental.getEndDateTime(),
                    rental.getTotalCost()
            );
        }).collect(Collectors.toList());
    }

    public List<RentalHistoryResponseDto> getMyRentals(String license){
        List<Rental> rentals = rentalRepository.findRentalHistory(license);
        return convertToHistoryDto(rentals);
    }

    // 해당 자동차의 예약내역확인
    public List<RentalHistoryResponseDto> getCarRentals(Long carId){
        List<Rental> rentals = rentalRepository.findRentalHistoryByCarId(carId);
        return convertToHistoryDto(rentals);

    }

    @Transactional
    public void rentalCar(RentalRequestDto rentalRequestDto, String license){
        RentCompletedEvent event = new RentCompletedEvent(license, rentalRequestDto.getCouponId());
        eventPublisher.publishEvent(event);

        rentalRepository.createRentalHistory(rentalRequestDto, license);
    }

}
