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
import team6.finalproject.domain.dm.entity.QMessage;

@RequiredArgsConstructor
public class DmRepositoryImpl implements DmRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<DmResponse> findDmListWithLastMessage(Long userId, String cursor, int limit, String keyword) {
		QMessage latestMsg = new QMessage("latestMsg");
		QMessage unreadCheck = new QMessage("unreadCheck");

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
				// 마지막 메시지 매핑
				Projections.constructor(MessageResponse.class,
					latestMsg.id, latestMsg.dmId, latestMsg.createdAt,
					Expressions.nullExpression(UserSimpleResponse.class),
					Expressions.nullExpression(UserSimpleResponse.class),
					latestMsg.content,
					latestMsg.isRead.coalesce(false)),
				// 읽지 않은 메시지 존재 여부 (unreadCheck 별칭 사용)
				JPAExpressions.selectOne()
					.from(unreadCheck)
					.where(unreadCheck.dmId.eq(dm.id)
						.and(unreadCheck.userId.ne(userId)) // 내가 보낸 게 아님
						.and(unreadCheck.isRead.isFalse()))   // 아직 안 읽음
					.exists()
			))
			.from(dm)
			.join(dmParticipant).on(dmParticipant.dmId.eq(dm.id))
			.join(user).on(user.id.eq(dmParticipant.userId))
			// 마지막 메시지만 1:1 조인하여 중복 제거
			.leftJoin(latestMsg).on(latestMsg.id.eq(
				JPAExpressions.select(unreadCheck.id.max())
					.from(unreadCheck)
					.where(unreadCheck.dmId.eq(dm.id))
			))
			.where(
				dm.id.in(myDmIds),
				dmParticipant.userId.ne(userId), // 상대방 정보만 추출
				cursorBefore(cursor),
				nameContains(keyword)
			)
			.orderBy(dm.id.desc())
			.limit(limit)
			.fetch();
	}

	@Override
	public Optional<DmResponse> findDmDetailById(Long dmId, Long currentUserId) {
		QMessage latestMsg = new QMessage("latestMsgDetail");

		return Optional.ofNullable(queryFactory
			.select(Projections.constructor(DmResponse.class,
				dm.id,
				Projections.constructor(UserSimpleResponse.class,
					user.id, user.name, user.profileImageUrl),

				// [수정] nullExpression 대신 진짜 최신 메시지 정보를 넣습니다.
				Projections.constructor(MessageResponse.class,
					latestMsg.id,
					latestMsg.dmId,
					latestMsg.createdAt,
					Expressions.nullExpression(UserSimpleResponse.class), // 프론트는 ID만 필요하므로 내부는 null이어도 됨
					Expressions.nullExpression(UserSimpleResponse.class),
					latestMsg.content,
					latestMsg.isRead
				),

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
			.leftJoin(latestMsg).on(latestMsg.id.eq(
				JPAExpressions.select(message.id.max())
					.from(message)
					.where(message.dmId.eq(dmId))
			))

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
				Projections.constructor(UserSimpleResponse.class, user.id, user.name, user.profileImageUrl),
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