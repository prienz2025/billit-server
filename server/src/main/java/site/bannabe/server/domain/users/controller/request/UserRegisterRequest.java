package site.bannabe.server.domain.users.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserRegisterRequest(

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotNull(message = "이메일은 필수 입력입니다.")
    String email,

    // 대문자, 소문자, 숫자, 특수문자가 모두 포함되어야한다면 @Pattern 어노테이션 추가
    // 최소/최대 길이 제한이 있다면 @Size 어노테이션 추가
    @NotNull(message = "비밀번호는 필수 입력입니다.")
    String password
) {

}