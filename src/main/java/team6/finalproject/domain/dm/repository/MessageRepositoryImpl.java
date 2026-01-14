package team6.finalproject.domain.dm.repository;

import static team6.finalproject.domain.dm.entity.QMessage.message;
import static team6.finalproject.domain.user.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.dm.dto.MessageResponse;
import team6.finalproject.domain.dm.dto.UserSimpleResponse;

@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<MessageResponse> findMessagesByCursor(Long dmId, Long cursor, int limit) {
		return queryFactory
			.select(Projections.constructor(MessageResponse.class,
				message.id,
				message.dmId,
				message.createdAt,
				// 발신자 정보
				Projections.constructor(UserSimpleResponse.class,
					user.id, user.name, user.profileImageUrl),
				null, // 수신자 정보 (필요시 추가 조인)
				message.content,
				message.isRead
			))
			.from(message)
			.join(user).on(user.id.eq(message.userId))
			.where(
				message.dmId.eq(dmId),
				idLessThan(cursor)
			)
			.orderBy(message.id.desc())
			.limit(limit)
			.fetch();
	}

	private BooleanExpression idLessThan(Long cursor) {
		return cursor == null ? null : message.id.lt(cursor);
	}
}