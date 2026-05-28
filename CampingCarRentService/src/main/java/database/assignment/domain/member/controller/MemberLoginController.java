package database.assignment.domain.member.controller;

import database.assignment.domain.member.dto.Member;
import database.assignment.domain.member.dto.MemberLoginDto;
import database.assignment.domain.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class MemberLoginController {

    private MemberService memberService;

    @Autowired
    public MemberLoginController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody MemberLoginDto dto){
        memberService.login(dto);
        return ResponseEntity.ok("로그인 성공");
    }
}
