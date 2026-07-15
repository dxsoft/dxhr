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

    private static final RowMapper<DictionaryFieldConfig> FIELD_CONFIG_MAPPER = (rs, rowNum) -> new DictionaryFieldConfig(
            SqlText.trim(rs.getString("tblname")),
            SqlText.trim(rs.getString("field_name")),
            SqlText.trim(rs.getString("field_cap")),
            SqlText.trim(rs.getString("dmlb")),
            SqlText.trim(rs.getString("field_type")));

    private static final RowMapper<DictionaryTreeNode> TREE_NODE_MAPPER = (rs, rowNum) -> {
        String code = SqlText.trim(rs.getString("bm"));
        String prefix = SqlText.trim(rs.getString("prefix"));
        return new DictionaryTreeNode(
                code,
                code != null && prefix != null && code.startsWith(prefix) ? code.substring(prefix.length()) : code,
                SqlText.trim(rs.getString("mc")),
                parentCode(prefix, code),
                rs.getInt("sfsy") == 1);
    };

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

    List<DictionaryFieldConfig> findFieldConfigs(String tableName) {
        return jdbcTemplate.query("""
                SELECT tblname, field_name, field_cap, dmlb, field_type
                FROM fldjbxx
                WHERE (:tableName IS NULL
                   OR tblname = :tableName
                   OR (:tableName = 'dryjbxx' AND tblname = 'ryjbxx'))
                  AND (TRIM(dmlb) <> '' OR UPPER(field_type) = 'D')
                ORDER BY sequence, field_name
                """, new MapSqlParameterSource("tableName", emptyToNull(tableName)), FIELD_CONFIG_MAPPER);
    }

    List<DictionaryTreeNode> findTreeNodes(String prefix) {
        String trimmedPrefix = emptyToNull(prefix);
        return jdbcTemplate.query("""
                SELECT :prefix AS prefix, bm, mc, czbm, xt, sfsy
                FROM dmb
                WHERE (:prefix IS NULL OR bm LIKE :prefixLike)
                  AND sfsy = 1
                ORDER BY bm
                """, new MapSqlParameterSource()
                .addValue("prefix", trimmedPrefix)
                .addValue("prefixLike", trimmedPrefix == null ? null : trimmedPrefix + "%"), TREE_NODE_MAPPER);
    }

    List<DictionaryTreeNode> findTreeNodesFiltered(DictionaryFilterSpec filter) {
        return jdbcTemplate.query("""
                SELECT :prefix AS prefix, bm, mc, czbm, xt, sfsy
                FROM dmb
                WHERE sfsy = 1
                  AND %s
                ORDER BY bm
                """.formatted(filter.whereClause()), new MapSqlParameterSource()
                .addValue("prefix", filter.treePrefix()), TREE_NODE_MAPPER);
    }

    String findOrganizationCategory(String organizationCode) {
        if (organizationCode == null || organizationCode.isBlank()) {
            return "";
        }
        List<String> values = jdbcTemplate.query("""
                SELECT dwbz
                FROM dwbm
                WHERE dwbm = :organizationCode
                LIMIT 1
                """, new MapSqlParameterSource("organizationCode", organizationCode.trim()),
                (rs, rowNum) -> SqlText.trim(rs.getString("dwbz")));
        return values.isEmpty() ? "" : values.getFirst();
    }

    boolean dictionaryExists(String code) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dmb WHERE bm = :code",
                new MapSqlParameterSource("code", code),
                Integer.class);
        return count != null && count > 0;
    }

    void insertDictionary(DictionaryMaintenanceRequest request) {
        jdbcTemplate.update("""
                INSERT INTO dmb (bm, mc, czbm, xt, sfsy)
                VALUES (:code, :name, :parentCode, :systemFlag, :enabledFlag)
                """, dictionaryParameters(request));
    }

    void updateDictionary(String code, DictionaryMaintenanceRequest request) {
        jdbcTemplate.update("""
                UPDATE dmb
                SET mc = :name,
                    czbm = :parentCode,
                    xt = :systemFlag,
                    sfsy = :enabledFlag
                WHERE bm = :code
                """, dictionaryParameters(request).addValue("code", code));
    }

    void disableDictionary(String code) {
        jdbcTemplate.update("""
                UPDATE dmb SET sfsy = 0 WHERE bm = :code
                """, new MapSqlParameterSource("code", code));
    }

    DictionaryEntry findDictionaryByCode(String code) {
        List<DictionaryEntry> rows = jdbcTemplate.query("""
                SELECT bm, mc, czbm, xt, sfsy FROM dmb WHERE bm = :code LIMIT 1
                """, new MapSqlParameterSource("code", code), DICTIONARY_ENTRY_MAPPER);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private MapSqlParameterSource dictionaryParameters(DictionaryMaintenanceRequest request) {
        return new MapSqlParameterSource()
                .addValue("code", request.code())
                .addValue("name", request.name())
                .addValue("parentCode", request.parentCode() == null ? "" : request.parentCode())
                .addValue("systemFlag", request.systemFlag() == null ? 0 : request.systemFlag())
                .addValue("enabledFlag", request.enabledFlag() == null ? 1 : request.enabledFlag());
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

    private static String parentCode(String prefix, String code) {
        if (prefix == null || code == null || code.length() <= prefix.length()) {
            return null;
        }
        int parentLength = code.length() > prefix.length() + 2 ? code.length() - 2 : prefix.length();
        return code.substring(0, parentLength);
    }
}
