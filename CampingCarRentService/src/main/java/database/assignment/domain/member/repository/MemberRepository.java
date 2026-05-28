package database.assignment.domain.member.repository;

import database.assignment.domain.member.dto.Member;
import database.assignment.domain.member.dto.MemberSignUpDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {
     //회원검색
    public Optional<Member> findMember(String name);

    //회원검색 by 면허증
    public Optional<Member> findMemberByLicense(String license);

    //모든회원 검색
    public List<Member> findAllMember();

    //회원추가
    public void createMember(MemberSignUpDto member);

}
