package database.assignment.domain.rent.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RentalRequestDto {
    private Long carId;
    private Long companyId;
    private Long couponId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int totalCost;
}
