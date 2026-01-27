package team6.finalproject.domain.notification.repository;

import static team6.finalproject.domain.notification.entity.QNotification.notification;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.notification.dto.CursorResponse;
import team6.finalproject.domain.notification.dto.NotificationDto;
import team6.finalproject.domain.notification.entity.Notification;
import team6.finalproject.domain.notification.entity.QNotification;

@RequiredArgsConstructor
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {

  private final JPAQueryFactory queryFactory;

  private static final String SORT_BY = "createdAt";

  @Override
  public CursorResponse<NotificationDto> findAll(Long userId, String cursor, Long idAfter, int limit,
      String sortDirection, String sortBy) {

    String sortB = SORT_BY;

    String sortD = normalizeSortDirection(sortDirection);
    boolean asc = "ASCENDING".equalsIgnoreCase(sortD);

    BooleanBuilder booleanBuilder = new BooleanBuilder();
    booleanBuilder.and(notification.user.id.eq(userId));

    BooleanExpression cursorExpr = cursorPredicate(cursor, idAfter, asc);
    if (cursorExpr != null) {
      booleanBuilder.and(cursorExpr);
    }

    List<OrderSpecifier<?>> orderSpecifiers = buildOrderSpecifiers(asc);

    List<NotificationDto> rows = queryFactory
        .select(Projections.constructor(
            NotificationDto.class,
            notification.id,
            notification.createdAt,
            notification.user.id,
            notification.title,
            notification.content,
            notification.level
        ))
        .from(notification)
        .where(booleanBuilder)
        .orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new))
        .limit(limit + 1L)
        .fetch();

    Long total = queryFactory
        .select(notification.count())
        .from(notification)
        .where(notification.user.id.eq(userId))
        .fetchOne();
    long totalCount = (total == null) ? 0L : total;

    boolean hasNext = rows.size() > limit;
    if (hasNext) {
      rows.remove(rows.size() - 1);
    }

    String nextCursor = null;
    Long nextIdAfter = null;

    if (hasNext && !rows.isEmpty()) {
      NotificationDto last = rows.get(rows.size() - 1);
      nextCursor = last.createdAt().toString();
      nextIdAfter = last.id();
    }

    return new CursorResponse<>(
        rows,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        sortB,
        sortD
    );
  }

  private BooleanExpression cursorPredicate(String cursor, Long idAfter, Boolean asc) {

    if (cursor == null || cursor.isBlank() || idAfter == null) {
      return null;
    }

    LocalDateTime c = LocalDateTime.parse(cursor);

    return asc
        ? notification.createdAt.gt(c).or(notification.createdAt.eq(c).and(notification.id.gt(idAfter)))
        : notification.createdAt.lt(c).or(notification.createdAt.eq(c).and(notification.id.lt(idAfter)));
  }

  private List<OrderSpecifier<?>> buildOrderSpecifiers(Boolean asc) {
    OrderSpecifier<?> primary =
        asc ? notification.createdAt.asc() : notification.createdAt.desc();

    OrderSpecifier<?> secondary = asc ? notification.id.asc() : notification.id.desc();

    return List.of(primary, secondary);

  }

  private String normalizeSortDirection(String sortDirection) {
    if (sortDirection == null || sortDirection.isBlank()) {
      return "DESCENDING";
    }

    return "ASCENDING".equalsIgnoreCase(sortDirection) ? "ASCENDING" : "DESCENDING";

  }
}
