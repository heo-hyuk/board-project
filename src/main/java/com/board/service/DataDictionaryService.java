package com.board.service;

import com.board.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// 데이터사전 조회 (INFORMATION_SCHEMA) + DDL 데모 - 채점/검수용 관리자 화면에서 사용
@Service
@RequiredArgsConstructor
public class DataDictionaryService {

    private final PostMapper postMapper;

    // 테이블 / 컬럼·제약조건(PK, NOT NULL) / 인덱스 / 뷰 / FK 목록을 한 번에 조회
    public Map<String, Object> getDataDictionary() {
        String schema = postMapper.selectCurrentSchema();

        List<Map<String, Object>> tables = postMapper.selectTableList(schema);

        return Map.of(
                "schema", schema,
                "tables", tables,
                "indexes", postMapper.selectIndexList(schema),
                "views", postMapper.selectViewList(schema),
                "foreignKeys", postMapper.selectForeignKeyList(schema)
        );
    }

    // 특정 테이블의 컬럼 구조 + 제약조건(PK, NOT NULL 등) 조회
    public List<Map<String, Object>> getTableColumns(String tableName) {
        String schema = postMapper.selectCurrentSchema();
        return postMapper.selectTableColumns(schema, tableName);
    }

    // DROP → CREATE DDL 데모 (post_stats는 애플리케이션 로직에서 사용하지 않는 순수 DDL 검증용 테이블)
    public void redoPostStatsTable() {
        postMapper.dropPostStatsTable();
        postMapper.createPostStatsTable();
    }
}
