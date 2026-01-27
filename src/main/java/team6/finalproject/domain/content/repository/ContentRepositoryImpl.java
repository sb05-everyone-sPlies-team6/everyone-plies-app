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
import team6.finalproject.domain.content.entity.content.SourceType;
import team6.finalproject.domain.content.entity.tag.QContentTag;
import team6.finalproject.domain.content.entity.tag.QTag;

@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	private BooleanExpression eqSourceType(String sourceStr) {
		if (sourceStr == null || sourceStr.isEmpty() || sourceStr.equalsIgnoreCase("all")) {
			return null;
		}

		try {
			SourceType sourceType = SourceType.valueOf(sourceStr.toUpperCase());
			return QContent.content.sourceType.eq(sourceType);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public List<Content> findAllByCursor(
		String cursor, String idAfter, int limit, List<String> tagsIn,
		String sortBy, String sortDirection, String typeEqual, String keywordLike, String sourceEqual) {

		QContent content = QContent.content;

		return queryFactory.selectFrom(content)
			.distinct()
			.leftJoin(QContentTag.contentTag).on(QContentTag.contentTag.content.eq(content))
			.leftJoin(QTag.tag).on(QContentTag.contentTag.tag.eq(QTag.tag))
			.where(
				cursorCondition(cursor, sortBy, sortDirection), // 복합 커서 조건
				eqType(typeEqual),
				eqSourceType(sourceEqual),
				containsKeyword(keywordLike),
				inTags(tagsIn) // 태그 필터링
			)
			.orderBy(getOrderSpecifier(sortBy, sortDirection))
			.limit(limit + 1)
			.fetch();
	}

	private BooleanExpression cursorCondition(String cursor, String sortBy, String sortDirection) {
		if (cursor == null || cursor.isBlank()) return null;

		QContent content = QContent.content;
		boolean isDesc = !"ASCENDING".equalsIgnoreCase(sortDirection);

		try {
			if ("rate".equals(sortBy)) {
				String[] parts = cursor.split("_");
				// IDE의 가이드대로 float로 파싱합니다.
				float lastRate = Float.parseFloat(parts[0]);
				long lastId = Long.parseLong(parts[1]); // lastId를 이 블록 안에서 선언

				return isDesc
					? content.totalRating.lt(lastRate).or(content.totalRating.eq(lastRate).and(content.contentId.lt(lastId)))
					: content.totalRating.gt(lastRate).or(content.totalRating.eq(lastRate).and(content.contentId.gt(lastId)));
			}

			if ("watcherCount".equals(sortBy)) {
				String[] parts = cursor.split("_");
				int lastCount = Integer.parseInt(parts[0]);
				long lastId = Long.parseLong(parts[1]); // lastId를 이 블록 안에서 선언

				return isDesc
					? content.totalReviews.lt(lastCount).or(content.totalReviews.eq(lastCount).and(content.contentId.lt(lastId)))
					: content.totalReviews.gt(lastCount).or(content.totalReviews.eq(lastCount).and(content.contentId.gt(lastId)));
			}

			// [CASE 3] 기본 ID 기반 커서
			long lastIdOnly = Long.parseLong(cursor);
			return isDesc ? content.contentId.lt(lastIdOnly) : content.contentId.gt(lastIdOnly);

		} catch (Exception e) {
			return null;
		}
	}

	// 2. 태그 필터링
	private BooleanExpression inTags(List<String> tagsIn) {
		if (tagsIn == null || tagsIn.isEmpty()) return null;
		return QTag.tag.name.in(tagsIn);
	}

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

	private BooleanExpression containsKeyword(String keyword) {
		return keyword != null ? QContent.content.title.containsIgnoreCase(keyword) : null;
	}

	private OrderSpecifier<?> getOrderSpecifier(String sortBy, String sortDirection) {
		QContent content = QContent.content;
		Order order = "ASCENDING".equalsIgnoreCase(sortDirection) ? Order.ASC : Order.DESC;

		if (sortBy == null) return new OrderSpecifier<>(Order.DESC, content.contentId);
		return switch (sortBy) {
			case "rate" -> new OrderSpecifier<>(order, content.totalRating);
			case "watcherCount" -> order == Order.DESC
				? new OrderSpecifier<>(Order.DESC, content.totalReviews)
				: new OrderSpecifier<>(Order.ASC, content.totalReviews);
			default -> new OrderSpecifier<>(order, content.contentId);
		};
	}
}