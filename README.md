# 🎵 Everyone's Playlist (모두의 플레이리스트) - API Server

글로벌 콘텐츠 큐레이션 플랫폼 '모두의 플레이리스트'의 핵심 비즈니스 로직을 담당하는 메인 API 서버입니다. TMDB 및 Sports DB Open API 연동을 통한 콘텐츠 수집과 사용자 맞춤형 플레이리스트, 실시간 소통 기능을 제공합니다.

## 🛠 Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.5.6
- **Security:** Spring Security, JWT, CSRF (Cookie-based)
- **Data:** Spring Data JPA, MySQL (RDS), Redis (ElastiCache)
- **Infrastructure:** AWS ECS (Fargate), AWS S3, CloudFront
- **Build Tool:** Gradle
- **Front-end:** Vite, React (Integrated via `/static`)

---

## 🏗 System Architecture

본 프로젝트는 확장성과 장애 격리를 위해 메인 API, 배치, 소켓 서버를 별도의 레포지토리로 분리하여 운영합니다.

- **API Server:** 메인 비즈니스 로직 및 프론트엔드 정적 파일 서빙
- **Batch Server:** Open API(TMDB 등)를 통한 주기적인 콘텐츠 데이터 수집 및 갱신
- **Socket Worker:** 실시간 채팅 및 시청 세션 관리를 위한 전용 서버

---

## ✨ Key Features

### 🔐 User & Auth

- **JWT & CSRF:** Stateless한 인증 구조와 쿠키 기반 CSRF(XSRF-TOKEN) 보안 강화
- **Admin:** 서버 기동 시 관리자 계정 자동 초기화 및 사용자 권한(ADMIN/USER) 관리
- **Security:** 계정 잠금 및 권한 변경 시 실시간 강제 로그아웃 처리, 임시 비밀번호 발급

### 🎬 Content & Curation

- **Data Integration:** TMDB, The Sports DB 연동을 통한 풍부한 콘텐츠 제공
- **Curation:** 개인별 플레이리스트 생성 및 타 사용자의 플레이리스트 구독 기능
- **Rating:** 콘텐츠별 평점 및 리뷰 시스템

### 💬 Communication & Real-time

- **SSE (Server-Sent Events):** 실시간 알림(구독, 팔로우, DM 수신) 제공
- **WebSocket:** 실시간 콘텐츠 같이 보기 시청자 정보 공유 및 채팅 (Socket 서버 연동)
- **DM:** 사용자 간의 실시간 쪽지 기능

---

## 📂 Project Structure

도메인을 기반으로 하여 각 기능의 응집도를 높였습니다.

```yaml
src/main/java/com/mopl/api/
├── global/                # 공통 설정 (Security, Exception, JWT)
└── domain/                # 도메인별 비즈니스 로직
    ├── auth/              # 인증 및 소셜 로그인
    ├── user/              # 사용자 및 팔로우 관리
    ├── content/           # 콘텐츠 및 태그 시스템
    ├── playlist/          # 플레이리스트 및 구독 관리
    └── communication/     # DM 및 알림(SSE) 시스템
```

---

## 📊 Database ERD

전체 데이터 구조는 사용자, 콘텐츠, 플레이리스트, 실시간 통신을 중심으로 설계되었습니다.

- **주요 테이블:** `Users`, `contents`, `playlists`, `Notifications`, `jwt_tokens` 등

---

## 🔗 API Documentation

Swagger UI를 통해 상세한 API 명세를 확인할 수 있습니다.

- **Swagger:** [API 명세서 바로가기](https://project.sb.sprint.learn.codeit.kr/sb/mopl/api/swagger-ui/index.html)
