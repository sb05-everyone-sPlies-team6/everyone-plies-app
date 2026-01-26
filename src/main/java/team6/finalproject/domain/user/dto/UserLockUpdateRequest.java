package team6.finalproject.domain.user.dto;

import jakarta.validation.constraints.NotNull;

public record UserLockUpdateRequest (
    @NotNull(message = "잠금 설정은 필수입니다.")
    boolean locked
) {

}
