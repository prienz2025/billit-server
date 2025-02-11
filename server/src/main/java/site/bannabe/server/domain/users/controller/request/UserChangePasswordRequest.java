package site.bannabe.server.domain.users.controller.request;

import jakarta.validation.constraints.NotNull;

public record UserChangePasswordRequest(
    @NotNull(message = "현재 비밀번호를 입력해주세요.")
    String currentPassword,
    @NotNull(message = "새 비밀번호를 입력해주세요.")
    String newPassword,
    @NotNull(message = "새 비밀번호 확인을 입력해주세요.")
    String newPasswordConfirm
) {

}