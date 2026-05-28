package database.assignment.domain.member.service;

import database.assignment.domain.member.dto.CustomUserDetails;
import database.assignment.domain.member.dto.Member;
import database.assignment.domain.member.repository.MemberRepository;
import database.assignment.global.error.BusinessException;
import database.assignment.global.exception.ErrorCode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomMemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomMemberDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member memberData = memberRepository.findMember(username)
                .orElseThrow(()->new BusinessException(ErrorCode.NOT_FOUND));

        return new CustomUserDetails(memberData);
    }
}
