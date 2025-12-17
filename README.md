# 릴스플레이스 (ReelsPlace) 백엔드

인스타그램 릴스에서 본 장소를 실제로 방문하게 만드는 연결 앱의 백엔드 서버

## 📋 목차
- [기술 스택](#기술-스택)
- [환경 설정](#환경-설정)
- [빌드 및 실행](#빌드-및-실행)
- [API 문서](#api-문서)

## 🛠 기술 스택

- **Java**: 17
- **Spring Boot**: 4.0.0
- **Database**: MariaDB
- **ORM**: JPA (Hibernate)
- **Security**: Spring Security + OAuth2 + JWT
- **Build Tool**: Gradle

## ⚙️ 환경 설정

### 1. 필수 요구사항
- Java 17 이상
- MariaDB 10.6 이상
- Gradle 8.x

### 2. 데이터베이스 생성
```sql
CREATE DATABASE reelsplace CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 환경변수 설정

프로젝트 루트에 `.env` 파일을 생성하세요:

```bash
# .env.example 파일을 복사
cp .env.example .env
```

`.env` 파일에 실제 값을 입력:

```properties
# Database
DB_PASSWORD=your_database_password

# Google OAuth2 (https://console.cloud.google.com/)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Kakao OAuth2 (https://developers.kakao.com/)
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret

# Naver OAuth2 (https://developers.naver.com/)
NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret

# JWT Secret (최소 256비트 이상)
JWT_SECRET=your_jwt_secret_key

# Server URL
SERVER_URL=http://localhost:8080
```

### 4. OAuth2 클라이언트 등록

#### Google
1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 프로젝트 생성 → API 및 서비스 → 사용자 인증 정보
3. OAuth 2.0 클라이언트 ID 생성
4. 승인된 리디렉션 URI 추가:
   - `http://localhost:8080/api/v1/auth/oauth2/callback/google`

#### Kakao
1. [Kakao Developers](https://developers.kakao.com/) 접속
2. 애플리케이션 추가 → 앱 설정 → 플랫폼
3. Redirect URI 설정:
   - `http://localhost:8080/api/v1/auth/oauth2/callback/kakao`
4. 동의항목 설정: 프로필 정보, 카카오계정(이메일)

#### Naver
1. [Naver Developers](https://developers.naver.com/) 접속
2. 애플리케이션 등록 → API 설정
3. Callback URL 설정:
   - `http://localhost:8080/api/v1/auth/oauth2/callback/naver`
4. 사용 API: 회원이름, 이메일주소

## 🚀 빌드 및 실행

### Gradle로 빌드
```bash
./gradlew clean build
```

### 실행
```bash
./gradlew bootRun
```

또는 JAR 파일로 실행:
```bash
java -jar build/libs/ReelsPlace-0.0.1-SNAPSHOT.jar
```

서버는 `http://localhost:8080`에서 실행됩니다.

## 📚 API 문서

### Base URL
```
http://localhost:8080/api/v1
```

### 인증
모든 API는 JWT 토큰 기반 인증이 필요합니다 (OAuth2 로그인 제외).

```
Authorization: Bearer {accessToken}
```

### 주요 엔드포인트

#### 🔐 인증 (Auth)
- `GET /auth/oauth2/authorization/{provider}` - OAuth2 로그인 시작
- `GET /users/me` - 내 정보 조회
- `PATCH /users/me/map-app` - 기본 지도 앱 변경
- `POST /auth/logout` - 로그아웃

#### 🎬 릴스 (Reel)
- `POST /reels` - 릴스 저장
- `GET /reels` - 내 릴스 목록 조회
- `DELETE /reels/{id}` - 릴스 삭제

#### 📍 장소 (Place)
- `GET /places` - 내 장소 목록 조회
- `DELETE /places/{id}` - 장소 삭제
- `POST /places/{id}/open-map` - 지도 앱 열기 기록

#### 📊 통계 (Stats)
- `GET /users/me/stats` - 마이페이지 통계

자세한 API 명세는 [Notion API 문서](노션링크)를 참고하세요.

## 🔒 보안 주의사항

- `.env` 파일은 절대 Git에 커밋하지 마세요
- 운영 환경에서는 환경변수를 서버 설정으로 관리하세요
- JWT Secret은 최소 256비트 이상의 랜덤 문자열을 사용하세요
- OAuth2 Client Secret은 절대 노출하지 마세요

## 📝 라이센스

이 프로젝트는 MIT 라이센스를 따릅니다.

## 👥 개발자

- Backend Developer: [Your Name]
