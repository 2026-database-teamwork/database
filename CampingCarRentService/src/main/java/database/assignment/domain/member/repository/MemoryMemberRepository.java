package database.assignment.domain.member.repository;

import database.assignment.domain.member.dto.Member;
import database.assignment.domain.member.dto.MemberSignUpDto;
import database.assignment.domain.member.dto.Role;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class MemoryMemberRepository implements MemberRepository{

    private final Map<String, Member> memberDB = new ConcurrentHashMap<>();

    @Override
    public Optional<Member> findMember(String name) {
        if(name==null) return Optional.empty();

        return memberDB.values().stream()
                .filter(member-> Objects.equals(member.getName(), name.trim()))
                .findFirst();
    }

    @Override
    public Optional<Member> findMemberByLicense(String license){
        if(license==null) return Optional.empty();

        return memberDB.values().stream()
                .filter(member->Objects.equals(member.getLicense(), license.trim()))
                .findFirst();
    }

    @Override
    public List<Member> findAllMember(){
        return memberDB.values().stream().collect(Collectors.toList());
    }

    @Override
    public void createMember(MemberSignUpDto requestMember) {
        Member member = new Member(
                requestMember.getLicense(),
                Role.USER,
                requestMember.getName(),
                requestMember.getPassword(),
                requestMember.getAddress(),
                requestMember.getPhone(),
                requestMember.getEmail() );

        memberDB.put(member.getLicense(), member);
    }

}
