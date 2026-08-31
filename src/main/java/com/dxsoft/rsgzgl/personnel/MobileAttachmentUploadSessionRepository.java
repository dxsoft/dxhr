package com.dxsoft.rsgzgl.personnel;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class MobileAttachmentUploadSessionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    MobileAttachmentUploadSessionRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    void ensureTables() {
        jdbc.getJdbcTemplate().execute("""
                CREATE TABLE IF NOT EXISTS app_mobile_attachment_upload_session (
                    token VARCHAR(64) NOT NULL PRIMARY KEY,
                    attachment_type VARCHAR(80) NOT NULL,
                    uid INT NOT NULL,
                    record_id VARCHAR(80) NULL,
                    public_base_url VARCHAR(512) NOT NULL,
                    expires_at TIMESTAMP NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbc.getJdbcTemplate().execute("""
                CREATE TABLE IF NOT EXISTS app_mobile_attachment_upload_file (
                    id VARCHAR(64) NOT NULL PRIMARY KEY,
                    session_token VARCHAR(64) NOT NULL,
                    stored_name VARCHAR(120) NOT NULL,
                    original_name VARCHAR(255) NOT NULL,
                    content_type VARCHAR(128) NOT NULL DEFAULT '',
                    file_size BIGINT NOT NULL DEFAULT 0,
                    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    consumed TINYINT(1) NOT NULL DEFAULT 0,
                    INDEX idx_mobile_upload_file_session (session_token)
                )
                """);
    }

    void insertSession(MobileAttachmentUploadSession session) {
        jdbc.update("""
                INSERT INTO app_mobile_attachment_upload_session (
                    token, attachment_type, uid, record_id, public_base_url, expires_at
                ) VALUES (
                    :token, :type, :uid, :recordId, :publicBaseUrl, :expiresAt
                )
                """, new MapSqlParameterSource()
                .addValue("token", session.token())
                .addValue("type", session.type())
                .addValue("uid", session.uid())
                .addValue("recordId", session.recordId())
                .addValue("publicBaseUrl", session.publicBaseUrl())
                .addValue("expiresAt", Timestamp.from(session.expiresAt())));
    }

    Optional<MobileAttachmentUploadSession> findByToken(String token) {
        List<MobileAttachmentUploadSession> sessions = jdbc.query("""
                SELECT token, attachment_type, uid, record_id, public_base_url, expires_at
                FROM app_mobile_attachment_upload_session
                WHERE token = :token
                """, new MapSqlParameterSource("token", token), (rs, rowNum) -> {
            Instant expiresAt = rs.getTimestamp("expires_at").toInstant();
            return new MobileAttachmentUploadSession(
                    rs.getString("token"),
                    rs.getString("attachment_type"),
                    rs.getInt("uid"),
                    rs.getString("record_id"),
                    rs.getString("public_base_url"),
                    expiresAt,
                    List.of());
        });
        if (sessions.isEmpty()) {
            return Optional.empty();
        }
        MobileAttachmentUploadSession session = sessions.getFirst();
        List<MobileAttachmentUploadFile> files = findFilesByToken(token);
        return Optional.of(session.withFiles(files));
    }

    List<MobileAttachmentUploadFile> findFilesByToken(String token) {
        return jdbc.query("""
                SELECT id, stored_name, original_name, content_type, file_size, uploaded_at, consumed
                FROM app_mobile_attachment_upload_file
                WHERE session_token = :token
                ORDER BY uploaded_at ASC, id ASC
                """, new MapSqlParameterSource("token", token), (rs, rowNum) -> new MobileAttachmentUploadFile(
                rs.getString("id"),
                rs.getString("stored_name"),
                rs.getString("original_name"),
                rs.getString("content_type"),
                rs.getLong("file_size"),
                rs.getTimestamp("uploaded_at").toInstant(),
                rs.getInt("consumed") != 0));
    }

    void insertFile(String sessionToken, MobileAttachmentUploadFile file) {
        jdbc.update("""
                INSERT INTO app_mobile_attachment_upload_file (
                    id, session_token, stored_name, original_name, content_type, file_size, uploaded_at, consumed
                ) VALUES (
                    :id, :sessionToken, :storedName, :originalName, :contentType, :fileSize, :uploadedAt, :consumed
                )
                """, new MapSqlParameterSource()
                .addValue("id", file.id())
                .addValue("sessionToken", sessionToken)
                .addValue("storedName", file.storedName())
                .addValue("originalName", file.originalName())
                .addValue("contentType", file.contentType())
                .addValue("fileSize", file.size())
                .addValue("uploadedAt", Timestamp.from(file.uploadedAt()))
                .addValue("consumed", file.consumed() ? 1 : 0));
    }

    void markFileConsumed(String sessionToken, String fileId) {
        jdbc.update("""
                UPDATE app_mobile_attachment_upload_file
                SET consumed = 1
                WHERE session_token = :sessionToken
                  AND id = :fileId
                """, new MapSqlParameterSource()
                .addValue("sessionToken", sessionToken)
                .addValue("fileId", fileId));
    }

    boolean markFileConsumedIfPending(String sessionToken, String fileId) {
        int updated = jdbc.update("""
                UPDATE app_mobile_attachment_upload_file
                SET consumed = 1
                WHERE session_token = :sessionToken
                  AND id = :fileId
                  AND consumed = 0
                """, new MapSqlParameterSource()
                .addValue("sessionToken", sessionToken)
                .addValue("fileId", fileId));
        return updated > 0;
    }

    List<MobileAttachmentUploadFile> deleteSession(String token) {
        List<MobileAttachmentUploadFile> files = new ArrayList<>(findFilesByToken(token));
        jdbc.update(
                "DELETE FROM app_mobile_attachment_upload_file WHERE session_token = :token",
                new MapSqlParameterSource("token", token));
        jdbc.update(
                "DELETE FROM app_mobile_attachment_upload_session WHERE token = :token",
                new MapSqlParameterSource("token", token));
        return files;
    }
}
