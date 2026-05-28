package database.assignment.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Member {
    private String license;
    private Role role; // 자바 내부 Enum을 쓰더라도 DB 값을 받기 위해 우선 String이나 매핑 로직 적용 가능
    private String name;
    private String password;
    private String address;
    private String phone;
    private String email;

    public Member(){}

}
