package database.assignment.domain.car.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Car {
    private Long carId;
    private Long companyId;
    private String carName;
    private String carNumber;
    private int numberOfRider;
    private String carImageUrl;
    private String carDetail;
    private int carRentalCost;

    public Car(){}
}