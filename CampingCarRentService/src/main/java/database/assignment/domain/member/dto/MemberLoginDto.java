package database.assignment.domain.member.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberLoginDto {
    private String name;
    private String password;
}
