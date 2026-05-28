package database.assignment.domain.repair.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CarRepairInfo {
    private Long repairId;
    private Long carId;
    private Long repairShopId;
    private String repairDetail;
    private LocalDateTime repairDate;
    private Integer repairCost;
}
