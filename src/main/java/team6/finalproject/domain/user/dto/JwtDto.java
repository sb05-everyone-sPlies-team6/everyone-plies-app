package team6.finalproject.domain.user.dto;

public record JwtDto (
  UserDto userDto,
  String accessToken
) {

}
