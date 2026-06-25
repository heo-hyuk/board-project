package com.board.mapper;

import com.board.dto.PostDto;
import com.board.dto.PostSearchDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

// MyBatis - 동적 쿼리 (검색 + 페이지네이션)
@Mapper
public interface PostMapper {

    // 게시글 목록 검색 (동적 WHERE절)
    List<PostDto> searchPosts(PostSearchDto searchDto);

    // 전체 게시글 수 (페이지네이션 계산용)
    int countPosts(PostSearchDto searchDto);
}
