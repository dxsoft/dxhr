package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.SqlText;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class SubrecordAttachmentRepository {

    private static final RowMapper<SubrecordAttachmentRecord> MAPPER = SubrecordAttachmentRepository::mapRow;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    SubrecordAttachmentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    List<SubrecordAttachmentRecord> findByRecord(String tableName, int recordId) {
        return findByKey(SubrecordAttachmentKey.forIntRecord(tableName, recordId));
    }

    List<SubrecordAttachmentRecord> findByKey(SubrecordAttachmentKey key) {
        return jdbcTemplate.query("""
                SELECT id, table_name, record_id, record_key, original_name, content_type, file_size, uploaded_by, created_at
                FROM app_subrecord_attachment
                WHERE table_name = :tableName
                  AND record_id = :recordId
                  AND record_key = :recordKey
                ORDER BY id
                """,
                keyParameters(key),
                MAPPER);
    }

    int countByKey(SubrecordAttachmentKey key) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM app_subrecord_attachment
                WHERE table_name = :tableName
                  AND record_id = :recordId
                  AND record_key = :recordKey
                """,
                keyParameters(key),
                Integer.class);
        return count == null ? 0 : count;
    }

    Optional<SubrecordAttachmentRecord> findById(long id) {
        return jdbcTemplate.query("""
                SELECT id, table_name, record_id, record_key, original_name, content_type, file_size, uploaded_by, created_at
                FROM app_subrecord_attachment
                WHERE id = :id
                """,
                new MapSqlParameterSource("id", id),
                MAPPER).stream().findFirst();
    }

    long insert(
            SubrecordAttachmentKey key,
            String originalName,
            String storedName,
            String contentType,
            long fileSize,
            String uploadedBy) {
        MapSqlParameterSource params = keyParameters(key)
                .addValue("originalName", originalName)
                .addValue("storedName", storedName)
                .addValue("contentType", contentType)
                .addValue("fileSize", fileSize)
                .addValue("uploadedBy", uploadedBy);
        jdbcTemplate.update("""
                INSERT INTO app_subrecord_attachment
                    (table_name, record_id, record_key, original_name, stored_name, content_type, file_size, uploaded_by)
                VALUES
                    (:tableName, :recordId, :recordKey, :originalName, :storedName, :contentType, :fileSize, :uploadedBy)
                """, params);
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Long.class);
        return id == null ? 0L : id;
    }

    Optional<String> findStoredName(long id) {
        return jdbcTemplate.query("""
                SELECT stored_name
                FROM app_subrecord_attachment
                WHERE id = :id
                """,
                new MapSqlParameterSource("id", id),
                (rs, rowNum) -> SqlText.trim(rs.getString("stored_name"))).stream().findFirst();
    }

    void deleteById(long id) {
        jdbcTemplate.update(
                "DELETE FROM app_subrecord_attachment WHERE id = :id",
                new MapSqlParameterSource("id", id));
    }

    List<String> deleteByKey(SubrecordAttachmentKey key) {
        List<String> storedNames = jdbcTemplate.query("""
                SELECT stored_name
                FROM app_subrecord_attachment
                WHERE table_name = :tableName
                  AND record_id = :recordId
                  AND record_key = :recordKey
                """,
                keyParameters(key),
                (rs, rowNum) -> SqlText.trim(rs.getString("stored_name")));
        jdbcTemplate.update("""
                DELETE FROM app_subrecord_attachment
                WHERE table_name = :tableName
                  AND record_id = :recordId
                  AND record_key = :recordKey
                """,
                keyParameters(key));
        return storedNames;
    }

    private static MapSqlParameterSource keyParameters(SubrecordAttachmentKey key) {
        return new MapSqlParameterSource()
                .addValue("tableName", key.tableName())
                .addValue("recordId", key.recordId())
                .addValue("recordKey", key.recordKey() == null ? "" : key.recordKey());
    }

    private static SubrecordAttachmentRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new SubrecordAttachmentRecord(
                rs.getLong("id"),
                SqlText.trim(rs.getString("table_name")),
                rs.getInt("record_id"),
                SqlText.trim(rs.getString("record_key")),
                SqlText.trim(rs.getString("original_name")),
                SqlText.trim(rs.getString("content_type")),
                rs.getLong("file_size"),
                SqlText.trim(rs.getString("uploaded_by")),
                rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toLocalDateTime());
    }
}
