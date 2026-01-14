# 🔐 로그인 인증 시스템 (Toy Project)

JWT + OAuth2 + Redis 기반 로그인 인증 시스템  
보안 중심 인증 구조를 직접 설계하고 구현한 개인 프로젝트

---

## 📌 프로젝트 개요

- **프로젝트명**: 로그인 인증 시스템 구축
- **개발 기간**: 2025.06 ~ 2025.07
- **개발 형태**: 1인 개발 (기획 → 구현 → 배포 전 과정 수행)
- **프로젝트 목표**
  - 로그인 / 인증 구조에 대한 실질적인 이해
  - 보안 이슈를 고려한 인증 시스템 설계 및 구현

---

## 🪄 기획 및 설계

### 기획 의도

- 사용자가 빠르고 직관적으로 로그인할 수 있는 구조
- 로컬 로그인 + 소셜 로그인(OAuth2) 통합
- 로그인 실패 시 계정 잠금 등 보안 정책 적용

기획 문서  
👉 https://www.notion.so/23be3f98ad7e80a4830ed67e69985a09?pvs=21

---

## 🔄 구현 과정에서의 주요 변경 사항

### 1️⃣ 인증 방식 전환

**기존**
- Session + LocalStorage 기반 인증

**문제**
- XSS 공격 시 토큰 탈취 위험
- Refresh Token 노출 가능성

**개선**
- JWT + HttpOnly Cookie 기반 인증 구조로 전환
- 무상태 인증 구조로 보안성과 확장성 확보

---

### 2️⃣ 로그인 실패 횟수 제한 구조 개선

**기존**
- 로그인 실패 횟수 MySQL 저장
- 요청마다 DB 접근으로 성능 저하

**개선**
- Redis로 로그인 실패 횟수 관리
- TTL(Time To Live) 적용으로 자동 잠금 해제
- 로그인 성공 시 Redis 실패 기록 즉시 초기화

---

### 3️⃣ 프론트 인증 흐름 개선

**기존**
- Thymeleaf 기반 SSR(Form Submit)

**개선**
- Ajax 기반 비동기 로그인 처리
- REST API + JSON 응답 구조로 전환
- 페이지 리로딩 없는 UX 유지

---

## 🧩 최종 설계 구조

### 🔐 JWT 인증 구조

- Access Token / Refresh Token 분리
  - Access Token: 15분
  - Refresh Token: 2일 (자동 로그인 시 14일)
- RSA 비대칭 키(RS256) 기반 서명
- email을 JWT subject로 사용
- HttpOnly + Secure 쿠키 저장
- HTTPS 환경에서만 전송

JwtTokenUtil  
👉 https://github.com/jhs980410/login/blob/main/login/src/main/java/com/assignment/login/auth/util/JwtTokenUtil.java

---

### 🧠 Redis 기반 로그인 제한 처리

- 로그인 실패 시 Redis에 실패 횟수 저장
- 5회 이상 실패 시 계정 잠금
- TTL 만료 시 자동 해제
- 로그인 성공 시 Redis 기록 삭제
- 잠금 상태일 경우 해제 예상 시간 반환

---

### 🌐 소셜 로그인 (OAuth2) 통합

- Spring Security 단일 설정으로 로컬 로그인 + OAuth2 처리
- Google / Kakao / Naver 지원
- 인증 방식과 무관하게 JWT 발급 로직 공통 적용

#### 인증 흐름 요약

**소셜 로그인**
1. `/oauth2/authorization/{provider}` 요청
2. Provider 인증
3. 사용자 정보 처리
4. 성공 시 JWT 발급 / 실패 시 에러 처리

**로컬 로그인**
1. `[POST] /api/auth/login`
2. Security Filter 인증 처리
3. 사용자 조회 및 계정 상태 검증
4. 성공 시 JWT 발급 / 실패 시 실패 횟수 증가

---

### 📧 이메일 인증

- 이메일 인증 및 비밀번호 찾기에 사용
- 인증 코드 생성 후 서버 메모리에 저장
- 이메일로 인증 코드 전송
- 입력값과 비교 후 검증

※ 현재 메모리 기반  
※ 추후 Redis 전환 예정

---

## 🛠️ 기술 스택

### Backend
- Java 17
- Spring Boot 3.5.3
- Spring Security 6.5.1
- Spring Data JPA
- JJWT 0.11.5

### Frontend
- Thymeleaf
- HTML / CSS
- Ajax

### DB / Cache
- MySQL 8
- Redis

### 인증
- JWT (Access / Refresh)
- OAuth2 (Google, Kakao, Naver)

### 배포
- AWS EC2 (Ubuntu 24.04)
- SCP + SSH 자동 배포 스크립트

---

## 🧱 DB 및 인증 구조 요약

- 사용자 / 로그인 실패 기록 / 토큰 정보 분리 저장
- 이메일 + 로그인 타입 기준 복합 유니크 키 적용
- 실패 횟수 및 Refresh Token Redis 관리
- JWT는 서버에서 서명 후 HttpOnly 쿠키로 전달

---

## 🧩 트러블슈팅

### 이메일 중복 및 소셜 로그인 충돌

- 동일 이메일로 로컬/소셜 계정 충돌 발생
- 이메일 + 로그인 타입 기준 중복 방지
- 기존 계정 존재 시 사용자 동의 후 연동

---

### LocalStorage 인증 구조 취약점

- JavaScript 접근 가능 → XSS 위험
- Refresh Token 노출 가능성
- HttpOnly Cookie 방식으로 전환하여 해결

---

### Redis recentLogin 우회 문제

- 인증 실패 후에도 최근 로그인 정보 갱신 가능
- 인증 성공 이후에만 최근 로그인 정보 갱신하도록 수정

---

## 🧠 프로젝트 회고

단순한 로그인 기능 구현을 넘어서  
보안과 운영 환경을 고려한 인증 시스템 설계를 목표로 진행한 프로젝트입니다.

JWT, OAuth2, Redis, RSA 비대칭 키 등  
실무에서 사용되는 인증 기술을 직접 설계하고 구현하며  
운영 가능한 구조에 대해 깊이 고민할 수 있었습니다.

실제 사용자 서비스는 아니었지만  
실서비스 환경을 가정한 다양한 시나리오를 고려하며  
백엔드 개발자로서 중요한 경험을 쌓을 수 있었습니다.
