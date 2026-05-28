package database.assignment.domain.member.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum Role {
    ADMIN("ROLE_ADMIN", "관리자"),
    USER("ROLE_USER", "일반 사용자");

    private final String key;
    private final String title;

    Role(String key, String title){
        this.key = key;
        this.title = title;
    }

    public String getKey() { return key; }

    // 📌 어떤 문자열이 들어와도 안전하게 Role을 찾아주는 메서드
    public static Role fromString(String roleStr) {
        if (roleStr == null) return Role.USER; // 기본값 설정

        // 대문자로 바꾸고, 혹시 앞 뒤에 공백이 있다면 제거
        String cleanStr = roleStr.toUpperCase().trim();

        // 만약 "ROLE_USER"처럼 들어왔다면 "USER"만 추출
        if (cleanStr.startsWith("ROLE_")) {
            cleanStr = cleanStr.replace("ROLE_", "");
        }

        try {
            return Role.valueOf(cleanStr);
        } catch (IllegalArgumentException e) {
            return Role.USER; // 일치하는 게 없으면 기본 권한으로 안전하게 대체
        }
    }
}
