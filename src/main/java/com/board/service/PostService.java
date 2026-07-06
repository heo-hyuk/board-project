package com.board.service;

import com.board.domain.Post;
import com.board.domain.PostLike;
import com.board.domain.Tag;
import com.board.domain.User;
import com.board.dto.PostDto;
import com.board.dto.PostSearchDto;
import com.board.mapper.PostMapper;
import com.board.repository.PostLikeRepository;
import com.board.repository.PostRepository;
import com.board.repository.TagRepository;
import com.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final TagRepository tagRepository;
    private final PostMapper postMapper;

    // 게시글 목록 검색 + 페이지네이션 (MyBatis)
    public Map<String, Object> searchPosts(PostSearchDto searchDto) {
        List<PostDto> posts = postMapper.searchPosts(searchDto);
        int totalCount = postMapper.countPosts(searchDto);
        int totalPages = (int) Math.ceil((double) totalCount / searchDto.getPageSize());

        Map<String, Object> result = new HashMap<>();
        result.put("posts", posts);
        result.put("totalCount", totalCount);
        result.put("totalPages", totalPages);
        result.put("currentPage", searchDto.getPage());
        return result;
    }

    // 게시글 상세 조회 + 조회수 증가 (JPA)
    @Transactional
    public Post findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        post.increaseViewCount();
        return post;
    }

    // 게시글 조회 (조회수 증가 없음) - 수정 페이지 등에서 사용
    public Post findByIdReadOnly(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
    }

    // 홈 화면 최신 포스트 6개 (JPA)
    public List<Post> getRecentPosts() {
        return postRepository.findTop6ByOrderByCreatedAtDesc();
    }

    // 게시글 작성 (MyBatis INSERT + JPA 태그 처리)
    @Transactional
    public Long write(String title, String summary, String content, String category, String tagString, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // MyBatis로 게시글 INSERT (useGeneratedKeys로 생성된 ID 획득)
        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        params.put("summary", (summary != null && !summary.isBlank()) ? summary.trim() : null);
        params.put("content", content);
        params.put("category", (category != null && !category.isBlank()) ? category : "Java");
        params.put("userId", user.getId());
        postMapper.insertPost(params);

        // MariaDB + MyBatis는 generated key를 BigInteger로 반환하므로 longValue()로 변환
        Long postId = ((Number) params.get("id")).longValue();

        // 태그 처리: JPA로 엔티티 로드 후 M2M 관계 설정
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 저장 실패"));
        post.updateTags(parseTags(tagString));
        return postId;
    }

    // 게시글 수정 (MyBatis UPDATE + JPA 태그 처리)
    @Transactional
    public void update(Long postId, String title, String summary, String content, String category, String tagString, String username) {
        // 권한 확인: JPA로 엔티티 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        // MyBatis로 게시글 주요 필드 UPDATE
        Map<String, Object> params = new HashMap<>();
        params.put("id", postId);
        params.put("title", title);
        params.put("summary", (summary != null && !summary.isBlank()) ? summary.trim() : null);
        params.put("content", content);
        params.put("category", (category != null && !category.isBlank()) ? category : "Java");
        postMapper.updatePost(params);

        // 태그 처리: JPA로 M2M 관계 업데이트
        post.updateTags(parseTags(tagString));
    }

    // 게시글 삭제 (MyBatis DELETE)
    @Transactional
    public void delete(Long postId, String username) {
        // 권한 확인: JPA로 엔티티 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        // MyBatis로 삭제 (post_tags FK 제약으로 인해 순서 중요)
        postMapper.deletePostTags(postId);  // 1. post_tags 먼저 삭제
        postMapper.deletePost(postId);      // 2. posts 삭제
    }

    // 내가 작성한 게시글 목록 (JPA)
    public List<Post> findMyPosts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return postRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // ===========================
    // 좋아요
    // ===========================

    // 좋아요 토글 - true: 좋아요 / false: 취소
    @Transactional
    public boolean toggleLike(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return postLikeRepository.findByPostAndUser(post, user)
                .map(like -> {
                    postLikeRepository.delete(like); // 이미 눌렀으면 취소
                    return false;
                })
                .orElseGet(() -> {
                    postLikeRepository.save(PostLike.create(post, user)); // 없으면 추가
                    return true;
                });
    }

    // 좋아요 수 조회
    public int getLikeCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        return postLikeRepository.countByPost(post);
    }

    // 현재 유저가 좋아요 눌렀는지 확인
    public boolean isLikedByUser(Long postId, String username) {
        if (username == null) return false;
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return false;
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;
        return postLikeRepository.existsByPostAndUser(post, user);
    }

    // ===========================
    // 태그 파싱 (내부 메서드)
    // ===========================
    private List<Tag> parseTags(String tagString) {
        if (tagString == null || tagString.isBlank()) return List.of();

        return Arrays.stream(tagString.split(","))
                .map(String::trim)
                .map(t -> t.startsWith("#") ? t.substring(1) : t) // # 제거
                .filter(t -> !t.isEmpty() && t.length() <= 20)
                .distinct()
                .limit(5) // 최대 5개
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(Tag.create(name)))) // 없으면 생성
                .collect(Collectors.toList());
    }
}
