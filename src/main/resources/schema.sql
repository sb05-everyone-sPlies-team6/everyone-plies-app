-- 1. 사용자 테이블
CREATE TABLE `users` (
                         `user_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                         `email`	VARCHAR(255)	NOT NULL UNIQUE COMMENT '로그인 계정 (중복 불가)',
                         `password`	VARCHAR(255),
                         `name`	VARCHAR(100)	NOT NULL,
                         `profile_image`	VARCHAR(500)	NULL,
                         `role`	VARCHAR(20)	NOT NULL DEFAULT 'USER' COMMENT 'ADMIN, USER',
                         `locked`	VARCHAR(20)	NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, LOCKED',
                        `provider` VARCHAR(50),
                        `provider_id` VARCHAR(255),
                         `created_at`	TIMESTAMP	NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at`	TIMESTAMP	NULL ON UPDATE CURRENT_TIMESTAMP,
                         `deleted_at`	TIMESTAMP	NULL,
                         PRIMARY KEY (`user_id`)
);

-- 2. 콘텐츠 테이블 (수정 제안 반영)
CREATE TABLE `contents` (
                            `content_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                            `title`	VARCHAR(255)	NOT NULL,
                            `type`	VARCHAR(50)	NOT NULL COMMENT 'MOVIE, DRAMA, SPORTS',
                            `description`	TEXT	NULL,
                            `thumbnail_url`	VARCHAR(500)	NULL,
                            `external_id`	VARCHAR(100)	NULL COMMENT 'TMDB 등 외부 ID',
                            `source_type`	VARCHAR(20)	NULL COMMENT 'TMDB, THE_SPORTS_DB, MANUAL',
                            `total_rating`	FLOAT	NULL DEFAULT 0.0,
                            `total_reviews`	INT	NULL DEFAULT 0,
                            PRIMARY KEY (`content_id`),
                            UNIQUE KEY `uk_external_source` (`external_id`, `source_type`) -- 중복 수집 방지 인덱스
);

-- 3. 태그 테이블
CREATE TABLE `tags` (
                        `tag_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                        `name`	VARCHAR(50)	NOT NULL UNIQUE,
                        PRIMARY KEY (`tag_id`)
);

-- 콘텐츠-태그 매핑
CREATE TABLE `contents_tags` (
                                 `mapping_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                 `content_id`	BIGINT	NOT NULL,
                                 `tag_id`	BIGINT	NOT NULL,
                                 PRIMARY KEY (`mapping_id`)
);

-- 플레이리스트-콘텐츠 매핑
CREATE TABLE `playlists_contents` (
                                      `mapping_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                      `playlist_id`	BIGINT	NOT NULL,
                                      `content_id`	BIGINT	NOT NULL,
                                      `created_at`	TIMESTAMP	NULL DEFAULT CURRENT_TIMESTAMP,
                                      PRIMARY KEY (`mapping_id`)
);

-- 소셜 로그인 연동
CREATE TABLE `social_auths` (
                                `social_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                `user_id`	BIGINT	NOT NULL,
                                `provider`	VARCHAR(50)	NOT NULL COMMENT 'KAKAO, GOOGLE',
                                `provider_user_id`	VARCHAR(255)	NOT NULL,
                                PRIMARY KEY (`social_id`)
);

-- JWT 토큰 관리
CREATE TABLE `jwt_tokens` (
                              `jwt_token_id`	VARCHAR(255)	NOT NULL, -- UUID 혹은 토큰값 자체
                              `user_id`	BIGINT	NOT NULL,
                              `cookie_name`	VARCHAR(255)	NULL,
                              `header_name`	VARCHAR(255)	NULL,
                              `created_at`	TIMESTAMP	NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `expires_at`	TIMESTAMP	NOT NULL,
                              `revoked`	BOOLEAN	NOT NULL DEFAULT FALSE,
                              PRIMARY KEY (`jwt_token_id`)
);

-- 팔로우
CREATE TABLE `follows` (
                           `follow_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                           `follower_id`	BIGINT	NOT NULL,
                           `following_id`	BIGINT	NOT NULL,
                           `created_at`	TIMESTAMP	NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (`follow_id`)
);

-- 대화방
CREATE TABLE `conversation` (
                                `conversation_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                `created_at`	TIMESTAMP	NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `name`	VARCHAR(50)	NULL,
                                PRIMARY KEY (`conversation_id`)
);

-- 대화 참여자 (공백 및 오타 수정)
CREATE TABLE `conversation_participants` (
                                             `participant_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                             `conversation_id`	BIGINT	NOT NULL,
                                             `user_id`	BIGINT	NOT NULL,
                                             `participant_chat`	VARCHAR(255)	NULL,
                                             `leave_chat`	VARCHAR(255)	NULL,
                                             `joined_at`	TIMESTAMP	DEFAULT CURRENT_TIMESTAMP,
                                             PRIMARY KEY (`participant_id`)
);

-- DM 메시지
CREATE TABLE `dm_message` (
                              `message_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                              `conversation_id`	BIGINT	NOT NULL,
                              `user_id`	BIGINT	NOT NULL,
                              `content`	TEXT	NOT NULL,
                              `created_at`	TIMESTAMP	NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `deleted_at`	TIMESTAMP	NULL,
                              PRIMARY KEY (`message_id`)
);

-- 콘텐츠 평점 및 리뷰
CREATE TABLE `reviews` (
                                   `rating_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                   `user_id`	BIGINT	NOT NULL,
                                   `content_id`	BIGINT	NOT NULL,
                                   `rating`	FLOAT	NULL DEFAULT 0.0,
                                   `text`	VARCHAR(1000)	NULL,
                                   PRIMARY KEY (`rating_id`)
);

-- 플레이리스트
CREATE TABLE `playlists` (
                             `playlist_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                             `user_id`	BIGINT	NOT NULL,
                             `title`	VARCHAR(255)	NOT NULL,
                             `description`	TEXT	NULL,
                             `total_subscription`	INT	NULL DEFAULT 0,
                             `created_at`	TIMESTAMP	NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at`	TIMESTAMP	NULL ON UPDATE CURRENT_TIMESTAMP,
                             `deleted_at`	TIMESTAMP	NULL,
                             PRIMARY KEY (`playlist_id`)
);

-- 플레이리스트 구독
CREATE TABLE `playlists_subscription` (
                                          `subscription_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                          `user_id`	BIGINT	NOT NULL,
                                          `playlist_id`	BIGINT	NOT NULL,
                                          `created_at`	TIMESTAMP	NULL DEFAULT CURRENT_TIMESTAMP,
                                          PRIMARY KEY (`subscription_id`)
);

-- 알림
CREATE TABLE `notifications` (
                                 `notification_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                 `user_id`	BIGINT	NOT NULL,
                                 `title`	VARCHAR(100)	NOT NULL,
                                 `content`	VARCHAR(500)	NOT NULL,
                                 `level`	VARCHAR(50)	NOT NULL COMMENT 'INFO, WARNING, ERROR',
                                 `is_read`	BOOLEAN	NOT NULL DEFAULT FALSE,
                                 `target_type`	VARCHAR(100)	NOT NULL COMMENT 'PLAYLIST, FOLLOW, DM',
                                 `target_id`	BIGINT	NOT NULL,
                                 `created_at`	TIMESTAMP	NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`notification_id`)
);

-- 콘텐츠 태그 관계
ALTER TABLE `contents_tags` ADD CONSTRAINT `FK_contents_TO_contents_tags` FOREIGN KEY (`content_id`) REFERENCES `contents` (`content_id`);
ALTER TABLE `contents_tags` ADD CONSTRAINT `FK_tags_TO_contents_tags` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`tag_id`);

-- 플레이리스트 콘텐츠 관계
ALTER TABLE `playlists_contents` ADD CONSTRAINT `FK_playlists_TO_playlists_contents` FOREIGN KEY (`playlist_id`) REFERENCES `playlists` (`playlist_id`);
ALTER TABLE `playlists_contents` ADD CONSTRAINT `FK_contents_TO_playlists_contents` FOREIGN KEY (`content_id`) REFERENCES `contents` (`content_id`);

-- DM 메시지 관계
ALTER TABLE `dm_message` ADD CONSTRAINT `FK_conversation_TO_dm_message` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`conversation_id`);
ALTER TABLE `dm_message` ADD CONSTRAINT `FK_users_TO_dm_message` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

-- 기타 주요 FK는 필요에 따라 추가하기