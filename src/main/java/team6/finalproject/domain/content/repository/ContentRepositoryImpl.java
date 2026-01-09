package team6.finalproject.domain.content.repository;

import java.util.List;

import com.querydsl.core.types.Order;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.content.ContentType;
import team6.finalproject.domain.content.entity.content.QContent;

@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<Content> findAllByCursor(Long cursor, int limit, String sortBy, String sortDirection, String typeEqual, String keywordLike) {
		QContent content = QContent.content;

		return queryFactory.selectFrom(content)
			.where(
				ltCursorId(cursor, sortBy, sortDirection), // 정렬 기준에 따른 커서 조건
				eqType(typeEqual),
				containsKeyword(keywordLike)
			)
			.orderBy(getOrderSpecifier(sortBy, sortDirection)) // 동적 정렬 적용
			.limit(limit + 1)
			.fetch();
	}

	private OrderSpecifier<?> getOrderSpecifier(String sortBy, String sortDirection) {
		QContent content = QContent.content;
		Order order = "ASCENDING".equalsIgnoreCase(sortDirection) ? Order.ASC : Order.DESC;

		if (sortBy == null) return new OrderSpecifier<>(Order.DESC, content.contentId);

		return switch (sortBy) {
			case "rate" -> new OrderSpecifier<>(order, content.totalRating);
			case "watcherCount" -> new OrderSpecifier<>(order, content.totalReviews); // 명세에 따라 필드 매핑
			default -> new OrderSpecifier<>(order, content.contentId); // createdAt 대용
		};
	}

	// 프론트의 소문자 타입을 백엔드 Enum으로 매핑하여 필터링
	private BooleanExpression eqType(String typeStr) {
		if (typeStr == null || typeStr.isEmpty() || typeStr.equalsIgnoreCase("all")) {
			return null;
		}

		ContentType type = switch (typeStr) {
			case "movie" -> ContentType.MOVIE;
			case "tvSeries" -> ContentType.DRAMA;
			case "sport" -> ContentType.SPORTS;
			default -> null;
		};

		return type != null ? QContent.content.type.eq(type) : null;
	}

	private BooleanExpression ltCursorId(Long cursorId, String sortBy, String sortDirection) {
		if (cursorId == null) return null;
		// 현재는 ID 기반 커서만 처리 (정렬 기준이 바뀌면 커서 로직도 복잡해지지만, 우선 ID 기준 유지)
		return QContent.content.contentId.lt(cursorId);
	}

	private BooleanExpression containsKeyword(String keyword) {
		return keyword != null ? QContent.content.title.containsIgnoreCase(keyword) : null;
	}
}