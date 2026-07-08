package com.board.service;

import com.board.domain.KnowledgePost;
import com.board.domain.User;
import com.board.dto.KnowledgePostDto;
import com.board.dto.KnowledgeSearchDto;
import com.board.exception.NotFoundException;
import com.board.mapper.KnowledgePostMapper;
import com.board.repository.KnowledgePostRepository;
import com.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgePostService {

    private final KnowledgePostRepository knowledgePostRepository;
    private final KnowledgePostMapper knowledgePostMapper;
    private final UserRepository userRepository;

    // 원본 글 목록 (페이지네이션 + 검색)
    public Map<String, Object> getRootPosts(KnowledgeSearchDto searchDto) {
        List<KnowledgePostDto> posts = knowledgePostMapper.selectRootPosts(searchDto);
        int totalCount = knowledgePostMapper.countRootPosts(searchDto);
        int totalPages = (int) Math.ceil((double) totalCount / searchDto.getPageSize());

        Map<String, Object> result = new HashMap<>();
        result.put("posts", posts);
        result.put("totalCount", totalCount);
        result.put("totalPages", Math.max(totalPages, 1));
        result.put("currentPage", searchDto.getPage());
        return result;
    }

    // 단건 조회 + 조회수 증가
    @Transactional
    public KnowledgePostDto findById(Long id) {
        KnowledgePostDto dto = knowledgePostMapper.selectById(id);
        if (dto == null) throw new NotFoundException("존재하지 않는 글입니다.");
        knowledgePostMapper.increaseViewCount(id);
        return dto;
    }

    // 단건 조회 (조회수 증가 없음) - 브랜치 폼 등에서 사용
    public KnowledgePostDto findByIdReadOnly(Long id) {
        KnowledgePostDto dto = knowledgePostMapper.selectById(id);
        if (dto == null) throw new NotFoundException("존재하지 않는 글입니다.");
        return dto;
    }

    // 원본 글의 브랜치 트리 (DFS 순서 평탄화 리스트로 반환)
    public List<KnowledgePostDto> getBranchesDfs(Long rootId) {
        List<KnowledgePostDto> all = knowledgePostMapper.selectBranchesByRootId(rootId);

        // parentId → 자식 목록 맵 구성
        Map<Long, KnowledgePostDto> idMap = new LinkedHashMap<>();
        for (KnowledgePostDto dto : all) {
            dto.setChildren(new ArrayList<>());
            idMap.put(dto.getId(), dto);
        }

        // 1차 브랜치(parentId = rootId) vs 중첩 브랜치 분리
        List<KnowledgePostDto> topLevel = new ArrayList<>();
        for (KnowledgePostDto dto : all) {
            if (dto.getParentId().equals(rootId)) {
                topLevel.add(dto);
            } else {
                KnowledgePostDto parent = idMap.get(dto.getParentId());
                if (parent != null) parent.getChildren().add(dto);
            }
        }

        // DFS 순서로 평탄화
        List<KnowledgePostDto> result = new ArrayList<>();
        dfsFlatten(topLevel, result);
        return result;
    }

    private void dfsFlatten(List<KnowledgePostDto> nodes, List<KnowledgePostDto> result) {
        for (KnowledgePostDto node : nodes) {
            result.add(node);
            dfsFlatten(node.getChildren(), result);
        }
    }

    // 원본 글 작성
    @Transactional
    public Long write(String title, String content, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        KnowledgePost post = KnowledgePost.createRoot(title, content, user);
        post = knowledgePostRepository.save(post);
        post.initRootId(); // rootId = 자기 자신 ID
        return post.getId();
    }

    // 브랜치 생성
    @Transactional
    public Long branch(Long parentId, String title, String content, String branchName, String username) {
        // 부모 글 확인
        KnowledgePostDto parentDto = knowledgePostMapper.selectById(parentId);
        if (parentDto == null) throw new NotFoundException("존재하지 않는 글입니다.");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        // 원본 글 ID: 부모가 원본이면 parentId, 아니면 부모의 rootId
        Long rootId = (parentDto.getParentId() == null) ? parentId : parentDto.getRootId();

        KnowledgePost branch = KnowledgePost.createBranch(
                title, content, branchName, user, parentId, rootId, parentDto.getDepth()
        );
        knowledgePostRepository.save(branch);
        return rootId; // 원본 글 페이지로 돌아가기 위해 rootId 반환
    }

    // 삭제 (작성자만 가능)
    @Transactional
    public Long delete(Long id, String username) {
        KnowledgePost post = knowledgePostRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 글입니다."));
        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        Long rootId = post.getRootId();
        boolean isRoot = (post.getParentId() == null);
        knowledgePostRepository.delete(post);

        // 원본 글 삭제 시 목록으로, 브랜치 삭제 시 원본 글로
        return isRoot ? null : rootId;
    }
}
