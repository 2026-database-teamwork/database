package database.assignment.domain.rent.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class RentalHistoryResponseDto {
    private String carName;
    private String license;
    private String companyName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int totalCost;
}