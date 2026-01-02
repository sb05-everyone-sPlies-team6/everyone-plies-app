package team6.finalproject.domain.user.dto;

public record SignupRequest (
    String name,
    String email,
    String password
) {

}
