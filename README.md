# News Scraper

네이버 뉴스 검색 API를 사용해 헬스 관련 뉴스를 조회하고, 결과를 콘솔에 출력하거나 GitHub Issue로 등록하는 Java 프로젝트입니다.

## 프로젝트 개요

이 프로젝트는 다음 흐름으로 동작합니다.

1. 사용자 입력 또는 환경변수에서 검색어와 검색 개수를 받습니다.
2. `NaverNewsProvider`가 네이버 뉴스 검색 API를 호출합니다.
3. 응답을 `NewsResult` 목록으로 변환합니다.
4. `NewsPublisher` 구현체가 결과를 출력하거나 GitHub Issue로 등록합니다.

## 주요 기능

- 네이버 뉴스 API 기반 검색
- 검색 결과 개수 제한
- 최신순 또는 정확도순 정렬
- 콘솔 출력
- GitHub Issue 자동 등록

## 실행 엔트리포인트

- `oop.search.presentation.ConsoleNewsApp`
- `oop.search.presentation.GitHubNewsApp`

## 프로젝트 구조

```text
src/
└── oop/search/
    ├── application/
    │   ├── NewsProvider.java
    │   ├── NewsPublisher.java
    │   └── NewsService.java
    ├── domain/
    │   ├── NewsCategory.java
    │   └── NewsResult.java
    ├── infrastructure/
    │   ├── AbstractHttpClient.java
    │   ├── NaverNewsProvider.java
    │   └── GitHubNewsPublisher.java
    └── presentation/
        ├── ConsoleNewsApp.java
        ├── ConsoleNewsPublisher.java
        └── GitHubNewsApp.java
```

## 환경 변수

### 네이버 뉴스 API

`NaverNewsProvider` 실행에 필요합니다.

- `NAVER_CLIENT_ID`
- `NAVER_CLIENT_SECRET`
- `NEWS_CATEGORY`

`NEWS_CATEGORY`는 `NewsCategory` enum 기준으로 설정합니다.

- `SIM` - 정확도순
- `DATE` - 최신순

### GitHub Issue 등록

`GitHubNewsPublisher` 실행에 필요합니다.

- `GITHUB_REPOSITORY`
- `GITHUB_TOKEN`

`GitHubNewsApp` 실행 시 추가로 사용합니다.

- `NEWS_QUERY`
- `NEWS_DISPLAY`

예시:

```bash
export NAVER_CLIENT_ID=your_client_id
export NAVER_CLIENT_SECRET=your_client_secret
export NEWS_CATEGORY=DATE

export GITHUB_REPOSITORY=owner/repo
export GITHUB_TOKEN=ghp_your_token
export NEWS_QUERY=헬스
export NEWS_DISPLAY=5
```

## 실행 방법

### 1. 콘솔 출력

`ConsoleNewsApp`을 실행하면 터미널에서 검색어와 검색 개수를 직접 입력할 수 있습니다.

동작 순서:

1. 검색어 입력
2. 검색 개수 입력
3. 네이버 뉴스 조회
4. 콘솔에 결과 출력

### 2. GitHub Issue 등록

`GitHubNewsApp`을 실행하면 `NEWS_QUERY`와 `NEWS_DISPLAY` 환경변수를 사용해 검색한 뒤, 결과를 GitHub Issue 본문으로 등록합니다.

## 도메인 모델

### `NewsResult`

현재 뉴스 한 건은 다음 정보를 가집니다.

- `title`
- `description`
- `url`
- `pubDate`
- `imageUrl`

### `NewsCategory`

- `SIM`
- `DATE`

## 동작 방식

`NewsService`가 `NewsProvider`와 `NewsPublisher`를 중개합니다.

- `NewsProvider`: 뉴스 데이터를 조회
- `NewsPublisher`: 조회 결과를 출력하거나 외부 시스템에 게시

이 구조 덕분에 데이터 조회와 출력 방식을 분리할 수 있습니다.

## 참고

- 네이버 뉴스 API 응답 형식은 네이버 개발자 문서를 따릅니다.
- GitHub Issue 등록은 GitHub REST API를 사용합니다.
