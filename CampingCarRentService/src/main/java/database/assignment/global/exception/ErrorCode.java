package database.assignment.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "MEM-001", "로그인 실패"),
    INVALID_VARIABLE(HttpStatus.BAD_REQUEST, "COM-001", "잘못된 요청양식"),
    DUPLICATE(HttpStatus.CONFLICT, "GLB-001", "중복된 값."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "GLB-002", "찾을 수 없음."),
    CREATE_ERROR(HttpStatus.CREATED, "GLB-003", "생성 실패.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message){
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus(){return status;}
    public String getCode() {return code;}
    public String getMessage() {return message;}
}
