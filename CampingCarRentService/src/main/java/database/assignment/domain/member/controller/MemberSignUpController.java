package database.assignment.domain.member.controller;

import database.assignment.domain.member.dto.MemberSignUpDto;
import database.assignment.domain.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class MemberSignUpController {

    private MemberService memberService;

    @Autowired
    public MemberSignUpController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody MemberSignUpDto dto){
        memberService.signUp(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }
}
