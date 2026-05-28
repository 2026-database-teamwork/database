package database.assignment.domain.member.jwt;

import database.assignment.domain.member.dto.CustomUserDetails;
import database.assignment.domain.member.dto.Member;
import database.assignment.domain.member.dto.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 요청 주소 확인
        String requestURI = request.getRequestURI();

        // 2. 📌 로그인이나 회원가입 경로라면 토큰 검사를 아예 하지 않고 다음 필터로 바로 넘깁니다.
        if (requestURI.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return; // 👈 여기서 메서드를 종료하여 아래의 "token null" 프린트까지 가지 않게 막습니다.
        }

        //request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        //Authorization 헤더 검증
        if(authorization == null || !authorization.startsWith("Bearer ")){
            System.out.println("token null");
            filterChain.doFilter(request, response);

            //조건에 해당되면 메소드 종료
            return;
        }

        System.out.println("authorization now");

        //Bearer 부분 제거후 순수 토큰만 획득
        String token = authorization.split(" ")[1];

        //토큰 소멸 시간 검증
        if(jwtUtil.isExpired(token)){
            System.out.println("token expired");
            filterChain.doFilter(request, response);

            //조건에 해당되면 메소드 종료(필수)
            return;
        }

        //토큰에서 username과 role획득
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        //userEntity를 생성하여 값 set
        Member member = new Member();
        member.setName(username);
        member.setPassword("temppassword");
        member.setRole(Role.fromString(role));

        //UserDetails에 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(member);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);

    }
}
