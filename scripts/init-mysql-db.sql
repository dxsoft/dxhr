-- MySQL Database Initialization Script for RSGZGL
-- Run this script to create the database and basic security tables

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS gzjsgl
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE gzjsgl;

-- Security tables (will be auto-created by app, but included here for reference)
CREATE TABLE IF NOT EXISTS app_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(120) NOT NULL,
    display_name VARCHAR(80) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    data_scope VARCHAR(20) NOT NULL DEFAULT 'CUSTOM'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(120) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(40) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_role_org_scope (
    role_id BIGINT NOT NULL,
    organization_code CHAR(9) NOT NULL,
    PRIMARY KEY (role_id, organization_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(80) NOT NULL UNIQUE,
    title VARCHAR(80) NOT NULL,
    path VARCHAR(120) NOT NULL,
    permission_code VARCHAR(120) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_security_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_username VARCHAR(80) NOT NULL,
    action VARCHAR(80) NOT NULL,
    target_type VARCHAR(40) NOT NULL,
    target_id VARCHAR(80) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Grant permissions (adjust username as needed)
-- GRANT ALL PRIVILEGES ON gzjsgl.* TO 'rsgzgl_user'@'localhost' IDENTIFIED BY 'your_password';
-- FLUSH PRIVILEGES;
