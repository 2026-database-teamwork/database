package database.assignment.domain.rent.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class Rental {
    private Long rentalId;
    private Long carId;
    private String license;
    private Long companyId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int totalCost;

    public Rental(){}
}

