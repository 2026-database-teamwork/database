package database.assignment.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSignUpDto {
    private String name;
    private String license;
    private String password;
    private String address;
    private String phone;
    private String email;

}
