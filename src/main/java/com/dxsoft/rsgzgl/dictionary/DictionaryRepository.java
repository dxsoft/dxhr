package com.dxsoft.rsgzgl.dictionary;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.SqlText;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class DictionaryRepository {

    private static final RowMapper<DictionaryEntry> DICTIONARY_ENTRY_MAPPER = (rs, rowNum) -> new DictionaryEntry(
            SqlText.trim(rs.getString("bm")),
            SqlText.trim(rs.getString("mc")),
            SqlText.trim(rs.getString("czbm")),
            rs.getInt("xt"),
            rs.getInt("sfsy"));

    private final NamedParameterJdbcTemplate jdbcTemplate;

    DictionaryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    List<DictionaryEntry> findEntries(String prefix, String keyword, PageRequest pageRequest) {
        MapSqlParameterSource parameters = parameters(prefix, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT bm, mc, czbm, xt, sfsy
                FROM dmb
                WHERE (:prefix IS NULL OR bm LIKE :prefixLike)
                  AND (:keyword IS NULL OR bm LIKE :keywordLike OR mc LIKE :keywordLike OR czbm = :keyword)
                ORDER BY bm
                LIMIT :limit OFFSET :offset
                """, parameters, DICTIONARY_ENTRY_MAPPER);
    }

    long countEntries(String prefix, String keyword) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dmb
                WHERE (:prefix IS NULL OR bm LIKE :prefixLike)
                  AND (:keyword IS NULL OR bm LIKE :keywordLike OR mc LIKE :keywordLike OR czbm = :keyword)
                """, parameters(prefix, keyword), Long.class);
        return count == null ? 0 : count;
    }

    private MapSqlParameterSource parameters(String prefix, String keyword) {
        String trimmedPrefix = emptyToNull(prefix);
        String trimmedKeyword = emptyToNull(keyword);
        return new MapSqlParameterSource()
                .addValue("prefix", trimmedPrefix)
                .addValue("prefixLike", trimmedPrefix == null ? null : trimmedPrefix + "%")
                .addValue("keyword", trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null ? null : "%" + trimmedKeyword + "%");
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
