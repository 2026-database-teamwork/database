package database.assignment.domain.member.service;

import database.assignment.domain.member.dto.Member;
import database.assignment.domain.member.dto.MemberLoginDto;
import database.assignment.domain.member.dto.MemberSignUpDto;
import database.assignment.domain.member.repository.MemberRepository;
import database.assignment.global.error.BusinessException;
import database.assignment.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.memberRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    //중복회원 확인 (중복일시:false)
    private boolean checkDuplicateMember(String name, String license){
        if(memberRepository.findMember(name).isPresent() &&
        memberRepository.findMemberByLicense(license).isPresent()) return true;
        return false;
    }

    //회원 불러오기
    public Member getMember(String name){
        Optional<Member> member = memberRepository.findMember(name);
        if(!member.isPresent()){
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return member.get();
    }

    //회원가입
    public void signUp(MemberSignUpDto member){
        if(checkDuplicateMember(member.getName(), member.getLicense())){
            throw new BusinessException(ErrorCode.DUPLICATE);
        }
        member.setPassword(bCryptPasswordEncoder.encode(member.getPassword()));
        memberRepository.createMember(member);
    }

    //로그인
    public void login(MemberLoginDto memberLoginDto){
        Member member = memberRepository.findMember(memberLoginDto.getName())
                .orElseThrow(()->new BusinessException(ErrorCode.NOT_FOUND));
        if(member.getPassword().equals(memberLoginDto.getPassword())){
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
    }

}
