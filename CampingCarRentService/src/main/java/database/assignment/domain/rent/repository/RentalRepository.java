package database.assignment.domain.rent.repository;

import database.assignment.domain.rent.dto.Rental;
import database.assignment.domain.rent.dto.RentalRequestDto;

import java.util.List;

public interface RentalRepository {
    public List<Rental> findRentalHistory(String license);

    List<Rental> findRentalHistoryByCarId(Long carId);

    public void createRentalHistory(RentalRequestDto request, String licencal);
}
