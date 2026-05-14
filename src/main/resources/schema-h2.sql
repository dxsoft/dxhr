-- H2 compatible schema for demonstration
-- Core tables and security tables

-- Security tables
CREATE TABLE IF NOT EXISTS app_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(120) NOT NULL,
    display_name VARCHAR(80) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS app_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    data_scope VARCHAR(20) NOT NULL DEFAULT 'CUSTOM'
);

CREATE TABLE IF NOT EXISTS app_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(120) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS app_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS app_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS app_role_org_scope (
    role_id BIGINT NOT NULL,
    organization_code CHAR(9) NOT NULL,
    PRIMARY KEY (role_id, organization_code)
);

CREATE TABLE IF NOT EXISTS app_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(80) NOT NULL UNIQUE,
    title VARCHAR(80) NOT NULL,
    path VARCHAR(120) NOT NULL,
    permission_code VARCHAR(120) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1
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

CREATE TABLE IF NOT EXISTS app_record_marker (
    table_name VARCHAR(80) NOT NULL,
    record_id VARCHAR(80) NOT NULL,
    marker VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (table_name, record_id, marker)
);

-- Core business tables
CREATE TABLE IF NOT EXISTS dwbm (
    dwbm VARCHAR(9) PRIMARY KEY,
    dwmc VARCHAR(80),
    dwmc1 VARCHAR(80),
    dwjc VARCHAR(40),
    dwbz VARCHAR(20),
    dwxz VARCHAR(20),
    dwsx VARCHAR(20),
    jxbl VARCHAR(10),
    gzczbz VARCHAR(20),
    gzlbbm VARCHAR(10),
    jbtbz VARCHAR(10),
    bzrs INTEGER DEFAULT 0,
    zbrs INTEGER DEFAULT 0,
    slrs INTEGER DEFAULT 0,
    dqrs INTEGER DEFAULT 0,
    dfbt VARCHAR(10),
    jxlb VARCHAR(10),
    njbtlb VARCHAR(10),
    jfly VARCHAR(10),
    kgjj INTEGER DEFAULT 0,
    kylbx INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS dryjbxx (
    uid INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    xm VARCHAR(20) NOT NULL,
    sfzh VARCHAR(18),
    xb VARCHAR(2),
    csny VARCHAR(7),
    ryfl VARCHAR(20),
    dwsx VARCHAR(10),
    gwfl VARCHAR(20),
    cjgzny VARCHAR(7),
    zzny VARCHAR(7),
    gznx INTEGER DEFAULT 0,
    xlbm VARCHAR(10),
    zgxl VARCHAR(20),
    zwjb VARCHAR(20),
    zjbm VARCHAR(10),
    xrzw VARCHAR(30),
    rzny VARCHAR(7),
    mz VARCHAR(10),
    zzmm VARCHAR(20),
    dah VARCHAR(20),
    xckhndzw VARCHAR(4),
    xckhndjb VARCHAR(4),
    UNIQUE KEY uk_dryjbxx_dwbm_grbm (dwbm, grbm)
);

CREATE TABLE IF NOT EXISTS hisbase (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    sid VARCHAR(40),
    uid INTEGER NOT NULL,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    xm VARCHAR(20),
    ny VARCHAR(6),
    bdlb VARCHAR(20),
    jsnf VARCHAR(4),
    jsyf VARCHAR(2),
    jslb VARCHAR(20),
    gw VARCHAR(20),
    zw VARCHAR(30),
    jb VARCHAR(10),
    dc VARCHAR(10),
    zwbm2 VARCHAR(20),
    zwgw2 VARCHAR(30),
    jbgzjb2 VARCHAR(10),
    zwgzdc2 VARCHAR(10),
    zwgz DECIMAL(10,2) DEFAULT 0,
    jbgz DECIMAL(10,2) DEFAULT 0,
    jsdjgz DECIMAL(10,2) DEFAULT 0,
    jxgz DECIMAL(10,2) DEFAULT 0,
    blfb DECIMAL(10,2) DEFAULT 0,
    jxjt DECIMAL(10,2) DEFAULT 0,
    njbt DECIMAL(10,2) DEFAULT 0,
    jhljt DECIMAL(10,2) DEFAULT 0,
    tggz DECIMAL(10,2) DEFAULT 0,
    fdgz DECIMAL(10,2) DEFAULT 0,
    jjjy DECIMAL(10,2) DEFAULT 0,
    pgbc DECIMAL(10,2) DEFAULT 0,
    hj DECIMAL(10,2) DEFAULT 0,
    zwgzse2 DECIMAL(10,2) DEFAULT 0,
    jbgzse2 DECIMAL(10,2) DEFAULT 0,
    jsdjgz2 DECIMAL(10,2) DEFAULT 0,
    dfbt2 DECIMAL(10,2) DEFAULT 0,
    blfb2 DECIMAL(10,2) DEFAULT 0,
    hj2 DECIMAL(10,2) DEFAULT 0,
    dq VARCHAR(2) DEFAULT ''
);

CREATE TABLE IF NOT EXISTS fldgz (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    sequence INTEGER,
    field_cate INTEGER DEFAULT 0,
    tblname VARCHAR(40) DEFAULT 'hisbase',
    field_name VARCHAR(40) NOT NULL,
    field_type VARCHAR(4) DEFAULT 'N',
    field_len INTEGER DEFAULT 10,
    field_dec INTEGER DEFAULT 2,
    field_cap VARCHAR(80),
    field_caps VARCHAR(80),
    field_capj VARCHAR(80),
    sfsy06 VARCHAR(4) DEFAULT '√',
    sfsy VARCHAR(4) DEFAULT '√',
    lrfs VARCHAR(20),
    category VARCHAR(40),
    jbt BOOLEAN DEFAULT FALSE,
    gld VARCHAR(40),
    jxryff INTEGER DEFAULT 0,
    jbtbz BOOLEAN DEFAULT FALSE,
    qsff VARCHAR(40),
    gdz DECIMAL(10,2) DEFAULT 0,
    readonly BOOLEAN DEFAULT FALSE,
    isgroup BOOLEAN DEFAULT FALSE,
    iscount BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS dryjbxxb (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    xm VARCHAR(20),
    sfzh VARCHAR(18),
    xb VARCHAR(2),
    bdny VARCHAR(6),
    bdlb VARCHAR(20),
    xgw VARCHAR(20),
    ngw VARCHAR(30),
    xhj DECIMAL(10,2) DEFAULT 0,
    gzbz VARCHAR(10),
    jbtbz VARCHAR(10),
    bz VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS hisbaseb (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    xm VARCHAR(20),
    ny VARCHAR(6),
    bdlb VARCHAR(20),
    gw VARCHAR(20),
    zw VARCHAR(30),
    jb VARCHAR(10),
    dc VARCHAR(10),
    zwgz DECIMAL(10,2) DEFAULT 0,
    jbgz DECIMAL(10,2) DEFAULT 0,
    jsdjgz DECIMAL(10,2) DEFAULT 0,
    jxgz DECIMAL(10,2) DEFAULT 0,
    blfb DECIMAL(10,2) DEFAULT 0,
    jxjt DECIMAL(10,2) DEFAULT 0,
    njbt DECIMAL(10,2) DEFAULT 0,
    jhljt DECIMAL(10,2) DEFAULT 0,
    tggz DECIMAL(10,2) DEFAULT 0,
    fdgz DECIMAL(10,2) DEFAULT 0,
    jjjy DECIMAL(10,2) DEFAULT 0,
    pgbc DECIMAL(10,2) DEFAULT 0,
    hj DECIMAL(10,2) DEFAULT 0
);

CREATE TABLE IF NOT EXISTS dryzwbh (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    rzzwbm VARCHAR(10),
    rzzw VARCHAR(30),
    zwjb VARCHAR(20),
    zjbm VARCHAR(10),
    gwbm VARCHAR(10),
    gwmc VARCHAR(30),
    gwlb VARCHAR(10),
    rzny VARCHAR(7),
    jgnx INTEGER DEFAULT 0,
    xrbz VARCHAR(2),
    jsbz VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS dxl (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    xl VARCHAR(10),
    xlmc VARCHAR(20),
    xx VARCHAR(40),
    rxsj VARCHAR(7),
    bysj VARCHAR(7),
    xz VARCHAR(10),
    xllb VARCHAR(10),
    bz VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS dndkh (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    xm VARCHAR(20),
    nd VARCHAR(4),
    khjg VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS jx (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    jx VARCHAR(20),
    sysj VARCHAR(7),
    syyy VARCHAR(20),
    rmwh VARCHAR(40),
    xrjxbz TINYINT DEFAULT 0,
    lb VARCHAR(2)
);

CREATE TABLE IF NOT EXISTS dtgxx (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    cjgzny VARCHAR(7),
    xlnx INTEGER DEFAULT 0,
    zdgznx INTEGER DEFAULT 0,
    kjnx INTEGER DEFAULT 0,
    tgnx INTEGER DEFAULT 0,
    zwbm VARCHAR(10),
    zwmc VARCHAR(30),
    rzsj VARCHAR(7),
    rznx INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS tgqgz2006 (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    xm VARCHAR(20),
    sfzh VARCHAR(18),
    xb VARCHAR(2),
    csny VARCHAR(7),
    ryfl VARCHAR(20),
    dwsx VARCHAR(10),
    gwfl VARCHAR(20),
    cjgzny VARCHAR(7),
    zzny VARCHAR(7)
);

CREATE TABLE IF NOT EXISTS hjxx (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    hjmc VARCHAR(40),
    sjdw VARCHAR(40),
    jllx VARCHAR(20),
    hjsj VARCHAR(7),
    tqyjjssj VARCHAR(10),
    qtqk VARCHAR(200),
    jldc INTEGER DEFAULT 0,
    jljb INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS dmb (
    bm VARCHAR(20) PRIMARY KEY,
    mc VARCHAR(40),
    czbm VARCHAR(20),
    sfbz INTEGER DEFAULT 0,
    sybz INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS cyxx (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9),
    dwmc VARCHAR(80),
    ds VARCHAR(20),
    sprq VARCHAR(10),
    gzbt VARCHAR(40),
    blxs INTEGER DEFAULT 0,
    blfs INTEGER DEFAULT 0,
    jxmc VARCHAR(20),
    shbtmc VARCHAR(20),
    spfs VARCHAR(10),
    dwshlb VARCHAR(10),
    jxgzms VARCHAR(10),
    jjjyms VARCHAR(10),
    fdgzms VARCHAR(10),
    pgbcms VARCHAR(10),
    zdbf INTEGER DEFAULT 0,
    jcgx INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS xtcs (
    qydrdk INTEGER DEFAULT 0,
    tgdcglbxl INTEGER DEFAULT 0,
    xsws INTEGER DEFAULT 0,
    jwbz INTEGER DEFAULT 0,
    tgjjjy INTEGER DEFAULT 0,
    fdgz INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS rptinfo (
    lbbm VARCHAR(3) PRIMARY KEY,
    cname VARCHAR(50) NOT NULL,
    ctitle VARCHAR(50) NOT NULL,
    cfilename VARCHAR(20) NOT NULL,
    rpttype VARCHAR(4) NOT NULL,
    bblb VARCHAR(8) NOT NULL,
    dyclb VARCHAR(8) NOT NULL,
    dycfw VARCHAR(5) NOT NULL,
    mrhs INTEGER NOT NULL DEFAULT 1,
    copies INTEGER NOT NULL DEFAULT 1,
    ptop DECIMAL(2,1) NOT NULL DEFAULT 0,
    ptop2 DECIMAL(2,1) NOT NULL DEFAULT 0,
    pleft DECIMAL(2,1) NOT NULL DEFAULT 0,
    pleft2 DECIMAL(2,1) NOT NULL DEFAULT 0,
    ptoph DECIMAL(2,1) NOT NULL DEFAULT 0,
    plefth DECIMAL(2,1) NOT NULL DEFAULT 0,
    para1 VARCHAR(50) NOT NULL DEFAULT '',
    para2 VARCHAR(8) NOT NULL DEFAULT '',
    para3 VARCHAR(10) NOT NULL DEFAULT '',
    para4 VARCHAR(2) NOT NULL DEFAULT '',
    reportname VARCHAR(8) NOT NULL DEFAULT '',
    haddb BOOLEAN NOT NULL DEFAULT FALSE,
    cdefault VARCHAR(10) NOT NULL DEFAULT '',
    lbmc VARCHAR(12) NOT NULL DEFAULT ''
);

-- Sample data for demonstration
INSERT INTO dwbm (dwbm, dwmc, dwmc1, dwjc, dwbz, dwxz, dwsx, jxbl, gzczbz, gzlbbm) VALUES
('001', '市委组织部', '组织部', '组织部', '机关', '机关', '机关', '', '', '001'),
('002', '市人社局', '人社局', '人社局', '机关', '机关', '机关', '', '', '001'),
('003', '市财政局', '财政局', '财政局', '事业', '事业', '事业', '7:3', '', '001');

INSERT INTO dryjbxx (dwbm, grbm, xm, sfzh, xb, csny, ryfl, cjgzny, gznx, xlbm, zgxl, xrzw, rzny, dah) VALUES
('001', '00001', '张三', '110101199001011234', '男', '1990.01', '公务员', '2010.01', 14, '001', '大学本科', '科员', '2020.01', 'A0001'),
('001', '00002', '李四', '110101198505056789', '女', '1985.05', '公务员', '2008.07', 16, '002', '硕士研究生', '副主任科员', '2018.03', 'A0002'),
('002', '00001', '王五', '110101197808082345', '男', '1978.08', '公务员', '2000.09', 24, '001', '大学本科', '科长', '2015.06', 'B0001'),
('003', '00001', '赵六', '110101198201012345', '男', '1982.01', '事业人员', '2005.04', 19, '001', '大学本科', '七级管理岗位', '2013.05', 'C0001');

INSERT INTO hisbase (
    sid, uid, dwbm, grbm, xm, ny, bdlb, jsnf, jsyf, jslb,
    gw, zw, jb, dc, zwbm2, zwgw2, jbgzjb2, zwgzdc2,
    zwgz, jbgz, jsdjgz, jxgz, blfb, jxjt, njbt, pgbc, hj,
    zwgzse2, jbgzse2, jsdjgz2, dfbt2, blfb2, hj2, dq
) VALUES
('', 1, '001', '00001', '张三', '202401', '正常晋升', '2024', '01', '正常晋升',
 '0701', '科员', '21', '5', '0701', '科员', '21', '5',
 1200, 2500, 0, 0, 300, 0, 120, 0, 5000,
 1200, 2500, 0, 880, 300, 5000, '是'),
('', 2, '001', '00002', '李四', '202401', '正常晋升', '2024', '01', '正常晋升',
 '0702', '副主任科员', '20', '8', '0702', '副主任科员', '20', '8',
 1500, 3200, 0, 0, 360, 0, 140, 0, 6000,
 1500, 3200, 0, 800, 360, 6000, '是'),
('', 3, '002', '00001', '王五', '202401', '正常晋升', '2024', '01', '正常晋升',
 '0703', '科长', '19', '10', '0703', '科长', '19', '10',
 1800, 3800, 0, 0, 420, 0, 160, 0, 7000,
 1800, 3800, 0, 820, 420, 7000, '是'),
('', 1, '001', '00001', '张三', '202301', '上次变动', '2023', '01', '上次变动',
 '0701', '科员', '21', '4', '0701', '科员', '21', '4',
 1100, 2400, 0, 0, 280, 0, 100, 0, 4700,
 1100, 2400, 0, 820, 280, 4700, ''),
('', 2, '001', '00002', '李四', '202301', '上次变动', '2023', '01', '上次变动',
 '0702', '副主任科员', '20', '7', '0702', '副主任科员', '20', '7',
 1400, 3000, 0, 0, 340, 0, 120, 0, 5600,
 1400, 3000, 0, 740, 340, 5600, ''),
('', 3, '002', '00001', '王五', '202301', '上次变动', '2023', '01', '上次变动',
 '0703', '科长', '19', '9', '0703', '科长', '19', '9',
 1700, 3600, 0, 0, 400, 0, 140, 0, 6600,
 1700, 3600, 0, 760, 400, 6600, ''),
('', 4, '003', '00001', '赵六', '202501', '正常晋升薪级', '2025', '01', '正常晋升薪级',
 'S07', '七级管理岗位', '31', '', 'S07', '七级管理岗位', '31', '',
 2520, 2582, 0, 0, 38, 0, 0, 0, 6449,
 2520, 2582, 0, 1305, 38, 6449, '是'),
('', 4, '003', '00001', '赵六', '202401', '上次变动', '2024', '01', '上次变动',
 'S07', '七级管理岗位', '30', '', 'S07', '七级管理岗位', '30', '',
 2520, 2475, 0, 0, 38, 0, 0, 0, 6342,
 2520, 2475, 0, 1305, 38, 6342, '');

INSERT INTO fldgz (sequence, field_name, field_cap, field_caps, field_capj, category) VALUES
(10, 'ZWGZSE2', '职务工资', '职务', '职务工资', '基本工资'),
(20, 'JBGZSE2', '级别/薪级工资', '级别', '级别工资', '基本工资'),
(30, 'JSDJGZ2', '技术等级工资', '技术', '技术等级', '基本工资'),
(40, 'DFBT2', '绩效/生活补贴', '绩效', '绩效/生活', '津贴补贴'),
(50, 'BLFB2', '保留福补', '福补', '保留福补', '津贴补贴'),
(60, 'JXJT', '警衔津贴', '警衔', '警衔津贴', '津贴补贴'),
(70, 'NJBT', '农教补贴', '农教', '农教补贴', '津贴补贴'),
(80, 'PGBC', '工改保留职务工资', '工改保留', '工改保留职务工资', '保留项'),
(90, 'JJJY2', '保留奖金', '奖金', '保留奖金', '保留项'),
(100, 'FDGZ2', '浮动工资', '浮动', '浮动工资', '津贴补贴'),
(110, 'JSFSZWTG2', '教护提高部分', '教护提高', '教护提高部分', '津贴补贴');

INSERT INTO dmb (bm, mc, czbm, sfbz, sybz) VALUES
('001', '大学本科', '', 1, 1),
('002', '硕士研究生', '', 1, 1),
('0701', '科员', '', 1, 1),
('0702', '副主任科员', '', 1, 1),
('0703', '科长', '', 1, 1);

INSERT INTO cyxx (dwbm, dwmc, ds, sprq, gzbt, jxmc, shbtmc) VALUES
('001', '市委组织部', '本地市', '2024-01-01', '公务员工资标准', '警衔津贴', '生活性补贴');

INSERT INTO xtcs (qydrdk, tgdcglbxl, xsws, jwbz, tgjjjy, fdgz) VALUES
(1, 1, 2, 1, 1, 1);

INSERT INTO jx (dwbm, grbm, jx, sysj, syyy, rmwh, xrjxbz, lb) VALUES
('001', '00001', '三级警督', '2020.01', '授予', '示例文号', 1, 'jx');

INSERT INTO dtgxx (dwbm, grbm, cjgzny, xlnx, zdgznx, kjnx, tgnx, zwbm, zwmc, rzsj, rznx) VALUES
('001', '00001', '2010.01', 0, 14, 14, 14, '0701', '科员', '2020.01', 4);

INSERT INTO tgqgz2006 (dwbm, grbm, xm, sfzh, xb, csny, ryfl, dwsx, gwfl, cjgzny, zzny) VALUES
('001', '00001', '张三', '110101199001011234', '男', '1990.01', '公务员', '机关', '综合管理', '2010.01', '2011.01');

INSERT INTO hjxx (dwbm, grbm, hjmc, sjdw, jllx, hjsj, tqyjjssj, qtqk, jldc, jljb) VALUES
('001', '00001', '嘉奖', '市委组织部', '奖励', '2022.01', '', '', 0, 0);

INSERT INTO rptinfo (lbbm, cname, ctitle, cfilename, rpttype, bblb, dyclb, dycfw, mrhs, copies, cdefault, lbmc) VALUES
('001', '机关正常档次晋升工资变动审批表', '河南省机关工作人员正常档次晋升工资变动审批表', 'jgjdspb', '工资', '审批表', '工资变动', '逐人', 1, 1, '国发2006-22号', '审批表'),
('002', '事业单位正常晋升薪级工资审批表', '河南省事业单位工作人员正常晋升薪级工资审批表', 'sydwxjspb', '工资', '审批表', '工资变动', '逐人', 1, 1, '国发2006-22号', '审批表'),
('003', '工资变动花名册', '工资变动花名册', 'gzbghmc', '工资', '花名册', '工资变动', '批量', 20, 1, '国发2006-22号', '花名册');

-- Security roles, permissions, menus and the admin user are seeded by
-- SecuritySchemaInitializer so the demo password uses the configured encoder.
