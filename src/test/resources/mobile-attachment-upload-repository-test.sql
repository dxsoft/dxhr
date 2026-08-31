CREATE TABLE app_mobile_attachment_upload_session (
    token VARCHAR(64) NOT NULL PRIMARY KEY,
    attachment_type VARCHAR(80) NOT NULL,
    uid INT NOT NULL,
    record_id VARCHAR(80) NULL,
    public_base_url VARCHAR(512) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE app_mobile_attachment_upload_file (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    session_token VARCHAR(64) NOT NULL,
    stored_name VARCHAR(120) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NOT NULL DEFAULT '',
    file_size BIGINT NOT NULL DEFAULT 0,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    consumed TINYINT NOT NULL DEFAULT 0
);
