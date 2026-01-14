package team6.finalproject.domain.dm.repository;

import static team6.finalproject.domain.dm.entity.QDm.dm;
import static team6.finalproject.domain.dm.entity.QDmParticipant.dmParticipant;
import static team6.finalproject.domain.dm.entity.QMessage.message;
import static team6.finalproject.domain.user.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import com.querydsl.core.types.dsl.Expressions;
import team6.finalproject.domain.dm.dto.DmResponse;
import team6.finalproject.domain.dm.dto.MessageResponse;
import team6.finalproject.domain.dm.dto.UserSimpleResponse;

@RequiredArgsConstructor
public class DmRepositoryImpl implements DmRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<DmResponse> findDmListWithLastMessage(Long userId, String cursor, int limit, String keyword) {
		List<Long> myDmIds = queryFactory
			.select(dmParticipant.dmId)
			.from(dmParticipant)
			.where(dmParticipant.userId.eq(userId))
			.fetch();

		if (myDmIds.isEmpty()) return List.of();

		return queryFactory
			.select(Projections.constructor(DmResponse.class,
				dm.id,
				Projections.constructor(UserSimpleResponse.class,
					user.id, user.name, user.profileImageUrl),
				// MessageResponse 내부의 null 처리
				Projections.constructor(MessageResponse.class,
					message.id, message.dmId, message.createdAt,
					Expressions.nullExpression(UserSimpleResponse.class), // 수정
					Expressions.nullExpression(UserSimpleResponse.class), // 수정
					message.content, message.isRead),
				JPAExpressions.selectOne()
					.from(message)
					.where(message.dmId.eq(dm.id)
						.and(message.userId.ne(userId))
						.and(message.isRead.isFalse()))
					.exists()
			))
			.from(dm)
			.join(dmParticipant).on(dmParticipant.dmId.eq(dm.id))
			.join(user).on(user.id.eq(dmParticipant.userId))
			.leftJoin(message).on(message.id.eq(
				JPAExpressions.select(message.id.max())
					.from(message)
					.where(message.dmId.eq(dm.id))
			))
			.where(
				dm.id.in(myDmIds),
				dmParticipant.userId.ne(userId),
				cursorBefore(cursor),
				nameContains(keyword)
			)
			.orderBy(dm.id.desc())
			.limit(limit)
			.fetch();
	}

	@Override
	public Optional<DmResponse> findDmDetailById(Long dmId, Long currentUserId) {
		return Optional.ofNullable(queryFactory
			.select(Projections.constructor(DmResponse.class,
				dm.id,
				Projections.constructor(UserSimpleResponse.class,
					user.id, user.name, user.profileImageUrl),
				Expressions.nullExpression(MessageResponse.class),
				JPAExpressions.selectOne()
					.from(message)
					.where(message.dmId.eq(dm.id)
						.and(message.userId.ne(currentUserId))
						.and(message.isRead.isFalse()))
					.exists()
			))
			.from(dm)
			.join(dmParticipant).on(dmParticipant.dmId.eq(dm.id))
			.join(user).on(user.id.eq(dmParticipant.userId))
			.where(
				dm.id.eq(dmId),
				dmParticipant.userId.ne(currentUserId)
			)
			.fetchOne());
	}

	@Override
	public List<MessageResponse> findMessagesByCursor(Long dmId, Long cursor, int limit) {
		return queryFactory
			.select(Projections.constructor(MessageResponse.class,
				message.id,
				message.dmId,
				message.createdAt,
				Projections.constructor(UserSimpleResponse.class,
					user.id, user.name, user.profileImageUrl),
				Expressions.nullExpression(UserSimpleResponse.class),
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
			.limit(limit + 1)
			.fetch();
	}

	private BooleanExpression cursorBefore(String cursor) {
		return cursor == null ? null : dm.id.lt(Long.parseLong(cursor));
	}

	private BooleanExpression idLessThan(Long cursor) {
		return cursor == null ? null : message.id.lt(cursor);
	}

	private BooleanExpression nameContains(String keyword) {
		return (keyword == null || keyword.isEmpty()) ? null : user.name.contains(keyword);
	}
}