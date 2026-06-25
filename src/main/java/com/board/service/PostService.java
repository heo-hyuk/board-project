package com.board.service;

import com.board.domain.Post;
import com.board.domain.User;
import com.board.dto.PostDto;
import com.board.dto.PostSearchDto;
import com.board.mapper.PostMapper;
import com.board.repository.PostRepository;
import com.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;   // JPA - CRUD
    private final UserRepository userRepository;
    private final PostMapper postMapper;            // MyBatis - 검색/페이지네이션

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

    // 게시글 작성 (JPA)
    @Transactional
    public Long write(String title, String content, String category, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Post post = Post.create(title, content, category, user);
        return postRepository.save(post).getId();
    }

    // 게시글 수정 (JPA)
    @Transactional
    public void update(Long postId, String title, String content, String category, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        // 작성자 본인 확인
        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        post.update(title, content, category);
    }

    // 게시글 삭제 (JPA)
    @Transactional
    public void delete(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        postRepository.delete(post);
    }

    // 내가 작성한 게시글 목록 (JPA)
    public List<Post> findMyPosts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return postRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
