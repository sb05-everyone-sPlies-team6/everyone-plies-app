package team6.finalproject.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record ResetPasswordRequest(
    @Email @NotNull
    String email
) {

}
