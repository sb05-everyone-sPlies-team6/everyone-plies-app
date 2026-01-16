package team6.finalproject.domain.user.dto;

public record JwtDto (
  JwtUserResponse userDto,
  String accessToken
) {

}
