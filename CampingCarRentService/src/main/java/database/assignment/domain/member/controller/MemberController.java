package database.assignment.domain.member.controller;

import database.assignment.domain.member.dto.Member;
import database.assignment.domain.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/member")
public class MemberController {
    private MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }


    @GetMapping("/{userId}")
    public ResponseEntity<Member> getUser(@PathVariable String userId){
        Member member = memberService.getMember(userId);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/")
    public String mainP(){
        return "Main Controller";
    }


}
