package com.learn.knowledge.repository;

import com.learn.knowledge.service.impl.DocumentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ChunkKeywordRepository {

    private static final String SQL = """
            WITH q AS (
                SELECT to_tsquery('english',
                       replace(plainto_tsquery('english', :question)::text, '&', '|')) AS query
            )
            SELECT v.id::text                    AS id,
                   v.content                     AS content,
                   v.metadata->>'fileName'       AS file_name,
                   v.metadata->>'documentId'     AS document_id,
                   v.metadata->>'chunk_index'    AS chunk_index,
                   ts_rank(to_tsvector('english', v.content), q.query) AS rank
            FROM vector_store v, q
            WHERE to_tsvector('english', v.content) @@ q.query
            ORDER BY rank DESC
            LIMIT :limit
            """;

    private final JdbcClient jdbcClient;

    public List<Document> search(String question, int limit) {
        return jdbcClient.sql(SQL)
                .param("question", question)
                .param("limit", limit)
                .query((rs, rowNum) -> {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put(DocumentServiceImpl.META_FILE_NAME, rs.getString("file_name"));
                    metadata.put(DocumentServiceImpl.META_DOCUMENT_ID, rs.getString("document_id"));
                    metadata.put("chunk_index", rs.getString("chunk_index"));
                    return Document.builder()
                            .id(rs.getString("id"))
                            .text(rs.getString("content"))
                            .metadata(metadata)
                            .score(rs.getDouble("rank"))
                            .build();
                })
                .list();
    }
}