package database.assignment.domain.company.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyRegisterDto{
    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String representativeName;
    private String representativeEmail;

    public CompanyRegisterDto(String companyName, String companyAddress, String companyPhone, String representativeName, String representativeEmail) {
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.companyPhone = companyPhone;
        this.representativeName = representativeName;
        this.representativeEmail = representativeEmail;
    }
}
