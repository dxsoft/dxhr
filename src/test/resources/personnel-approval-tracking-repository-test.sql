CREATE TABLE IF NOT EXISTS dwbm (
    dwbm VARCHAR(9) PRIMARY KEY,
    dwmc VARCHAR(80)
);

CREATE TABLE IF NOT EXISTS dryjbxx (
    uid INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    xm VARCHAR(20) NOT NULL,
    bbz VARCHAR(20),
    tjr VARCHAR(80),
    tjsj TIMESTAMP,
    shr VARCHAR(80),
    shsj TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dxl (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    xl VARCHAR(40),
    byyx VARCHAR(80),
    bysj VARCHAR(10),
    bbz VARCHAR(20),
    tjr VARCHAR(80),
    tjsj TIMESTAMP,
    shr VARCHAR(80),
    shsj TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dryzwbh (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    xzzw VARCHAR(40),
    xrzw VARCHAR(40),
    srny VARCHAR(10),
    bbz VARCHAR(20),
    tjr VARCHAR(80),
    tjsj TIMESTAMP,
    shr VARCHAR(80),
    shsj TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dndkh (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    khnd VARCHAR(4),
    khjg VARCHAR(20),
    bbz VARCHAR(20),
    tjr VARCHAR(80),
    tjsj TIMESTAMP,
    shr VARCHAR(80),
    shsj TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hjxx (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    hjmc VARCHAR(80),
    hjsj VARCHAR(10),
    bbz VARCHAR(20),
    tjr VARCHAR(80),
    tjsj TIMESTAMP,
    shr VARCHAR(80),
    shsj TIMESTAMP
);

CREATE TABLE IF NOT EXISTS jx (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    jx VARCHAR(40),
    sysj VARCHAR(10),
    bbz VARCHAR(20),
    tjr VARCHAR(80),
    tjsj TIMESTAMP,
    shr VARCHAR(80),
    shsj TIMESTAMP
);

CREATE TABLE IF NOT EXISTS app_security_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_username VARCHAR(80) NOT NULL,
    action VARCHAR(80) NOT NULL,
    target_type VARCHAR(40) NOT NULL,
    target_id VARCHAR(80) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS app_subrecord_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    table_name VARCHAR(40) NOT NULL,
    record_id INT NOT NULL,
    record_key VARCHAR(80) NOT NULL DEFAULT '',
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128),
    file_size BIGINT NOT NULL,
    uploaded_by VARCHAR(80),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
