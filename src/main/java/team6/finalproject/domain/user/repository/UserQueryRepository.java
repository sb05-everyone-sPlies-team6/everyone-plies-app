package team6.finalproject.domain.user.repository;

import java.util.List;
import team6.finalproject.domain.user.dto.CursorResponse;
import team6.finalproject.domain.user.dto.UserDto;
import team6.finalproject.domain.user.entity.Role;
import team6.finalproject.domain.user.entity.User;

public interface UserQueryRepository {

  CursorResponse<UserDto> findAll(String emailLike, Role role, Boolean isLocked,
      String cursor, Long idAfter, int limit, String sortDirection, String sortBy);

}
