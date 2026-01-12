package team6.finalproject.domain.user.repository;

import static team6.finalproject.domain.user.entity.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.user.dto.CursorResponse;
import team6.finalproject.domain.user.dto.UserDto;
import team6.finalproject.domain.user.entity.Role;
import team6.finalproject.domain.user.entity.User;

@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public CursorResponse<UserDto> findAll(String emailLike, Role role, Boolean isLocked,
      String cursor, Long idAfter, int limit, String sortDirection, String sortBy) {

    String sortB = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
    String sortD = (sortDirection == null || sortDirection.isBlank()) ? "ASCENDING" : sortDirection;

    BooleanBuilder booleanBuilder = new BooleanBuilder();

    if (emailLike != null && !emailLike.isBlank()) {
      booleanBuilder.and(user.email.containsIgnoreCase(emailLike));
    }

    if (role != null) {
      booleanBuilder.and(user.role.eq(role));
    }

    if (isLocked != null) {
      booleanBuilder.and(user.locked.eq(isLocked));
    }

    booleanBuilder.and(cursorPredicate(emailLike, role, isLocked,
        cursor, idAfter, limit, sortDirection, sortBy, sortB, sortD));

    List<OrderSpecifier<?>> orderSpecifiers = buildOrderSpecifiers(sortB, sortD);

    List<UserDto> users = queryFactory
        .select(Projections.constructor(
            UserDto.class,
            user.id,
            user.createdAt,
            user.email,
            user.name,
            user.profileImageUrl,
            user.role,
            user.locked))
        .from(user)
        .where(booleanBuilder)
        .orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new))
        .limit(limit + 1L)
        .fetch();

    Long total = queryFactory
        .select(user.count())
        .from(user)
        .where(buildFilterOnlyPredicate(emailLike, role, isLocked,
            cursor, idAfter, limit, sortDirection, sortBy))
        .fetchOne();

    long totalCount = (total != null) ? total : 0L;
    boolean hasNext = users.size() > limit;

    String nextCursor = null;
    Long nextIdAfter = null;

    if (hasNext) {
      users.remove(users.size() - 1);
      UserDto last = users.get(users.size() - 1);

      nextCursor = extractCursorValue(last, sortB);
      nextIdAfter = last.id();
    }

    CursorResponse<UserDto> result =
        new CursorResponse<>(users, nextCursor, nextIdAfter, hasNext, totalCount, sortB, sortD);

    return result;
  }



  private BooleanExpression cursorPredicate(String emailLike, Role role, Boolean isLocked,
      String cursor, Long idAfter, int limit, String sortDirection, String sortBy, String sortB, String sortD) {

    if (cursor == null || cursor.isBlank() || idAfter == null) {
      return null;
    }

    boolean asc = "ASCENDING".equalsIgnoreCase(sortDirection);

    return switch (sortB) {
      case "createdAt" -> {
        LocalDateTime after = LocalDateTime.parse(cursor);
        yield asc
            ? user.createdAt.gt(after).or(user.createdAt.eq(after).and(user.id.gt(idAfter)))
            : user.createdAt.lt(after).or(user.createdAt.eq(after).and(user.id.gt(idAfter)));
      }

      case "email" -> asc
          ? user.email.gt(cursor).or(user.email.eq(cursor).and(user.id.gt(idAfter)))
          : user.email.lt(cursor).or(user.email.eq(cursor).and(user.id.gt(idAfter)));

      case "name" -> asc
          ? user.name.gt(cursor).or(user.name.eq(cursor).and(user.id.gt(idAfter)))
          : user.name.lt(cursor).or(user.name.eq(cursor).and(user.id.gt(idAfter)));

      case "role" -> asc
          ? user.role.stringValue().gt(cursor).or(user.role.stringValue().eq(cursor).and(user.id.gt(idAfter)))
          : user.role.stringValue().lt(cursor).or(user.role.stringValue().eq(cursor).and(user.id.gt(idAfter)));

      case "isLocked" -> asc
          ? user.locked.stringValue().gt(cursor).or(user.locked.stringValue().eq(cursor).and(user.id.gt(idAfter)))
          : user.locked.stringValue().lt(cursor).or(user.locked.stringValue().eq(cursor).and(user.id.gt(idAfter)));

      default -> {
        LocalDateTime after = LocalDateTime.parse(cursor);
        yield asc
            ? user.createdAt.gt(after).or(user.createdAt.eq(after).and(user.id.gt(idAfter)))
            : user.createdAt.lt(after).or(user.createdAt.eq(after).and(user.id.gt(idAfter)));
      }
    };
  }
  private List<OrderSpecifier<?>> buildOrderSpecifiers(String sortBy, String sortDirection) {

    boolean asc = "ASC".equalsIgnoreCase(sortDirection);

    OrderSpecifier<?> primary = switch (sortBy) {
      case "createdAt" -> asc ? user.createdAt.asc() : user.createdAt.desc();
      case "email" -> asc ? user.email.asc() : user.email.desc();
      case "name" -> asc ? user.name.asc() : user.name.desc();
      case "role" -> asc ? user.role.asc() : user.role.desc();
      case "isLocked" -> asc ? user.locked.asc() : user.locked.desc();
      default -> asc ? user.createdAt.asc() : user.createdAt.desc();
    };

    return List.of(primary, user.id.asc());
  }

  private BooleanExpression buildFilterOnlyPredicate(String emailLike, Role role, Boolean isLocked,
      String cursor, Long idAfter, int limit, String sortDirection, String sortBy) {

    BooleanBuilder booleanBuilder = new BooleanBuilder();

    if (emailLike != null && !emailLike.isBlank()) {
      booleanBuilder.and(user.email.containsIgnoreCase(emailLike));
    }
    if (role != null) {
      booleanBuilder.and(user.role.eq(role));
    }
    if (isLocked != null) {
      booleanBuilder.and(user.locked.eq(isLocked));
    }

    return (BooleanExpression) booleanBuilder.getValue();
  }

  private String extractCursorValue(UserDto last, String sortBy) {
    return switch (sortBy) {
      case "createdAt" -> last.createdAt().toString();
      case "email" -> last.email();
      case "name" -> last.name();
      case "role" -> String.valueOf(last.role());
      case "isLocked" -> String.valueOf(last.locked());
      default -> last.createdAt().toString();
    };
  }
}
