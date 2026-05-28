package database.assignment.domain.car.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarRegisterDto {
    private int companyId;
    private String carName;
    private String carNumber;
    private int numberOfRider;
    private String carImageUrl;
    private String carDetail;
    private int carRentalCost;
}
