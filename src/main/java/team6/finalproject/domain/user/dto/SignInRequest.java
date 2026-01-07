package team6.finalproject.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record SignInRequest(
    @NotBlank
    String username,

    @NotBlank
    String password
) {

}
