package database.assignment.domain.company.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Company {
    private Long companyId;
    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String representativeName;
    private String representativeEmail;
}
