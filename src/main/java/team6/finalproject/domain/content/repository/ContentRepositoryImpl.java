package team6.finalproject.domain.content.repository;

import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.content.QContent;

// 2. 구현체 작성
@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<Content> findAllByCursor(Long cursor, int size, String sortBy, String sortDirection, String type,
		String keyword) {
		QContent content = QContent.content;

		return queryFactory.selectFrom(content)
			.where(ltCursorId(cursor)) // cursor보다 작은 ID들 조회 (최신순)
			.orderBy(content.contentId.desc())
			.limit(size + 1) // 다음 페이지 확인용으로 1개 더 조회
			.fetch();
	}

	private BooleanExpression ltCursorId(Long cursorId) {
		if (cursorId == null) return null;
		return QContent.content.contentId.lt(cursorId);
	}
}