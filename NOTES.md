# 작업 노트

## 현재 상태 (2026-06-29)

게시판 → 기술 블로그 플랫폼(CodeLog) 전환 완료

### 완료된 작업
- 마크다운 에디터(EasyMDE) + 렌더링(marked.js + highlight.js + DOMPurify) 적용
- 홈 페이지 히어로 섹션 + 포스트 카드 그리드 UI
- 기술 카테고리 (Java / Spring / Web / DevOps / 알고리즘 / CS지식 / 회고)
- Post에 summary 필드, User에 bio 필드 추가
- 마이페이지 개발자 프로필 스타일로 개편
- AWS Lightsail 배포 중
- README 업데이트 완료

---

## 배포 정보

| 항목 | 값 |
|------|-----|
| 서버 | AWS Lightsail (서울 리전) |
| IP | 43.201.55.31 |
| URL | http://43.201.55.31:8080 |
| PEM 키 | C:\aws\LightsailDefaultKey-ap-northeast-2.pem |

### 배포 명령
```bash
bash deploy.sh
```

### 로그 확인
```bash
ssh -i "C:\aws\LightsailDefaultKey-ap-northeast-2.pem" ubuntu@43.201.55.31 'tail -f /home/ubuntu/app/app.log'
```

---

## 다음 작업 목록 (우선순위 순)

1. **테스트 코드** — JUnit5 + Mockito, @DataJpaTest, @WebMvcTest (커버리지 70%+)
2. **GitHub Actions CI/CD** — PR → 빌드 → 테스트 → 배포 자동화
3. **REST API + Swagger** — `/api/v1/posts` 레이어 추가, springdoc-openapi 문서화
4. **JWT 인증** — 세션 기반 → Access Token + Refresh Token 전환
5. **Redis 캐싱** — 조회수, 인기글 `@Cacheable` 적용
