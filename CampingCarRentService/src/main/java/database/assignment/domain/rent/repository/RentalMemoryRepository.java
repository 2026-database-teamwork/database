package database.assignment.domain.rent.repository;

import database.assignment.domain.rent.dto.Rental;
import database.assignment.domain.rent.dto.RentalRequestDto;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class RentalMemoryRepository implements RentalRepository{
    private final Map<Long, Rental> rentalDB = new ConcurrentHashMap<>();
    public final AtomicLong sequence =new AtomicLong(0L);

    @Override
    public List<Rental> findRentalHistory(String license) {
        String targetLicense = (license != null) ? license.trim() : "";
        return rentalDB.values().stream()
                .filter(r-> Objects.equals(r.getLicense(), targetLicense))
                .collect(Collectors.toList());
    }

    @Override
    public List<Rental> findRentalHistoryByCarId(Long carId){
        return rentalDB.values().stream()
                .filter(r->r.getCarId()==carId)
                .collect(Collectors.toList());
    }

    @Override
    public void createRentalHistory(RentalRequestDto request, String license) {
        Rental rent = new Rental();
        rent.setRentalId(sequence.incrementAndGet());
        rent.setCarId(request.getCarId());
        rent.setLicense(license);
        rent.setCompanyId(request.getCompanyId());
        rent.setStartDateTime(request.getStartDateTime());
        rent.setEndDateTime(request.getEndDateTime());
        rent.setTotalCost(request.getTotalCost());

        rentalDB.put(rent.getRentalId(), rent);
    }
}
