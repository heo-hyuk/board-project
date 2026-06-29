# 작업 노트

---

## 배포 정보

| 항목 | 값 |
|------|-----|
| 서버 | AWS Lightsail (서울 리전) |
| IP | 43.201.55.31 |
| URL | http://43.201.55.31:8080 |
| PEM 키 | C:\aws\LightsailDefaultKey-ap-northeast-2.pem |

### 배포 명령 (로컬 → 서버)
```bash
# GitHub push 후 서버에서 직접 실행
ssh -i "C:\aws\LightsailDefaultKey-ap-northeast-2.pem" ubuntu@43.201.55.31 \
  "cd ~/board-project && git pull origin main && mvn clean package -DskipTests -q && pkill -f 'java -jar'; sleep 2 && nohup java -jar target/board-project-0.0.1-SNAPSHOT.jar > ~/app/app.log 2>&1 &"
```

### 로그 확인
```bash
ssh -i "C:\aws\LightsailDefaultKey-ap-northeast-2.pem" ubuntu@43.201.55.31 "tail -f ~/app/app.log"
```

### 앱만 재시작
```bash
ssh -i "C:\aws\LightsailDefaultKey-ap-northeast-2.pem" ubuntu@43.201.55.31 \
  "pkill -f 'java -jar'; sleep 2 && nohup java -jar /home/ubuntu/board-project/target/board-project-0.0.1-SNAPSHOT.jar > /home/ubuntu/app/app.log 2>&1 </dev/null &"
```

---

## 다음 작업 목록 (우선순위 순)

1. **테스트 코드** — JUnit5 + Mockito, @DataJpaTest, @WebMvcTest (커버리지 70%+)
2. **GitHub Actions CI/CD** — push → 빌드 → 테스트 → 서버 자동 배포
3. **REST API + Swagger** — `/api/v1/posts` 레이어 추가, springdoc-openapi 문서화
4. **JWT 인증** — 세션 기반 → Access Token + Refresh Token 전환
5. **Redis 캐싱** — 조회수, 인기글 `@Cacheable` 적용

---

## 작업 이력

### 2026-06-29 (2차)
**능력단위 평가 기준 충족 작업**
- `PostMapper.java` + `PostMapper.xml` 에 게시글 CRUD 추가 (MyBatis)
  - INSERT(useGeneratedKeys), SELECT(JOIN+서브쿼리), UPDATE, DELETE
- DDL 명령문 Mapper에 추가 (CREATE TABLE, ALTER TABLE, DROP TABLE)
- INFORMATION_SCHEMA 데이터사전 조회 추가 (테이블/컬럼/인덱스/뷰 목록)
- 인덱스 2개 생성: `idx_posts_category`, `idx_posts_created_at`
- 뷰 생성: `v_post_summary` (posts + users JOIN)
- `DatabaseInitializer.java` 신규 — 앱 시작 시 DDL 자동 실행
- `PostService.java` — 게시글 등록/수정/삭제를 JPA → MyBatis로 전환 (태그 처리만 JPA 유지)
- `schema.sql` 전체 스키마 문서화

**반응형 햄버거 메뉴 수정**
- 메뉴가 햄버거 아래 드롭다운으로 표시 (position: absolute; top: 100%)
- 항목 왼쪽 정렬 (align-items: stretch)
- 로그인/비로그인 영역 구분선 추가
- 글쓰기·회원가입·로그아웃 버튼 모바일 재스타일

---

### 2026-06-29 (1차)
**게시판 → 기술 블로그 플랫폼(CodeLog) 전환**
- 마크다운 에디터(EasyMDE) + 렌더링(marked.js + highlight.js + DOMPurify)
- 홈 히어로 섹션 + 포스트 카드 그리드 UI
- 기술 카테고리 (Java / Spring / Web / DevOps / 알고리즘 / CS지식 / 회고)
- Post에 summary 필드, User에 bio 필드 추가
- 마이페이지 개발자 프로필 스타일로 개편
- AWS Lightsail 배포
