package site.bannabe.server.domain.users.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserChangeNicknameRequest(
    @NotBlank
    @Size(min = 2, max = 20)
    String nickname
) {

}