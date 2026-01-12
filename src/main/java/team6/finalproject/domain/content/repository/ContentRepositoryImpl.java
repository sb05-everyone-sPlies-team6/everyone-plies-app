package team6.finalproject.domain.content.repository;

import java.util.List;
import java.util.UUID;

import com.querydsl.core.types.Order;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.content.ContentType;
import team6.finalproject.domain.content.entity.content.QContent;
import team6.finalproject.domain.content.entity.tag.QContentTag;
import team6.finalproject.domain.content.entity.tag.QTag;

@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<Content> findAllByCursor(
		String cursor,
		UUID idAfter,
		int limit,
		List<String> tagsIn,
		String sortBy,
		String sortDirection,
		String typeEqual,
		String keywordLike) {

		QContent content = QContent.content;
		QContentTag contentTag = QContentTag.contentTag;
		QTag tag = QTag.tag;

		return queryFactory.selectFrom(content)
			.distinct() // 태그 조인 시 발생하는 데이터 중복 제거
			.leftJoin(contentTag).on(contentTag.content.eq(content))
			.leftJoin(tag).on(contentTag.tag.eq(tag))
			.where(
				cursorCondition(cursor, idAfter, sortBy, sortDirection), // 문자열 커서 조건
				eqType(typeEqual),
				containsKeyword(keywordLike),
				inTags(tagsIn) // [추가] 태그 필터 조건
			)
			.orderBy(getOrderSpecifier(sortBy, sortDirection))
			.limit(limit + 1)
			.fetch();
	}

	private BooleanExpression inTags(List<String> tagsIn) {
		if (tagsIn == null || tagsIn.isEmpty()) {
			return null;
		}
		return QTag.tag.name.in(tagsIn);
	}

	// 커서 조건 처리 (String 타입을 Long으로 변환)
	private BooleanExpression cursorCondition(String cursor, UUID idAfter, String sortBy, String sortDirection) {
		if (cursor == null || cursor.isBlank()) return null;

		try {
			long cursorId = Long.parseLong(cursor);
			// 기본적으로 contentId 기준 내림차순(DESC)일 경우 lt(less than) 사용
			return QContent.content.contentId.lt(cursorId);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private OrderSpecifier<?> getOrderSpecifier(String sortBy, String sortDirection) {
		QContent content = QContent.content;
		Order order = "ASCENDING".equalsIgnoreCase(sortDirection) ? Order.ASC : Order.DESC;

		if (sortBy == null) return new OrderSpecifier<>(Order.DESC, content.contentId);

		return switch (sortBy) {
			case "rate" -> new OrderSpecifier<>(order, content.totalRating);
			case "watcherCount" -> new OrderSpecifier<>(order, content.totalReviews);
			default -> new OrderSpecifier<>(order, content.contentId);
		};
	}

	// 프론트의 소문자 타입을 백엔드 Enum으로 매핑하여 필터링
	private BooleanExpression eqType(String typeStr) {
		if (typeStr == null || typeStr.isEmpty() || typeStr.equalsIgnoreCase("all")) return null;

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