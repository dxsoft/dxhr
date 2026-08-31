CREATE TABLE dwbm (
    dwbm VARCHAR(9) PRIMARY KEY,
    dwmc VARCHAR(80),
    dwbz VARCHAR(80),
    dwsx VARCHAR(40),
    gzczbz VARCHAR(40),
    jkjs BOOLEAN DEFAULT FALSE
);

CREATE TABLE dryjbxx (
    uid INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    xm VARCHAR(20) NOT NULL,
    sfzh VARCHAR(20),
    xb VARCHAR(4),
    csny VARCHAR(10),
    ryfl VARCHAR(20),
    dwsx VARCHAR(20),
    gwfl VARCHAR(20),
    cjgzny VARCHAR(10),
    zzny VARCHAR(10),
    gznx INTEGER DEFAULT 0,
    xlbm VARCHAR(10),
    zgxl VARCHAR(20),
    zwjb VARCHAR(20),
    zjbm VARCHAR(20),
    xrzw VARCHAR(40),
    srny VARCHAR(10),
    mz VARCHAR(20),
    zzmm VARCHAR(20),
    dah VARCHAR(40),
    jrny VARCHAR(10),
    jrfs VARCHAR(20),
    bbz VARCHAR(20),
    tc VARCHAR(20),
    bz VARCHAR(200),
    txsj VARCHAR(10),
    tjr VARCHAR(80),
    tjsj TIMESTAMP,
    shr VARCHAR(80),
    shsj TIMESTAMP
);

CREATE TABLE dryjbxxb (
    uid INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    xm VARCHAR(20) NOT NULL,
    sfzh VARCHAR(20),
    xb VARCHAR(4),
    csny VARCHAR(10),
    ryfl VARCHAR(20),
    dwsx VARCHAR(20),
    gwfl VARCHAR(20),
    cjgzny VARCHAR(10),
    zzny VARCHAR(10),
    gznx INTEGER DEFAULT 0,
    xlbm VARCHAR(10),
    zgxl VARCHAR(20),
    zwjb VARCHAR(20),
    zjbm VARCHAR(20),
    xrzw VARCHAR(40),
    srny VARCHAR(10),
    mz VARCHAR(20),
    zzmm VARCHAR(20),
    dah VARCHAR(40),
    jrny VARCHAR(10),
    jrfs VARCHAR(20),
    bbz VARCHAR(20),
    tc VARCHAR(20),
    bz VARCHAR(200),
    txsj VARCHAR(10),
    tjr VARCHAR(80),
    tjsj TIMESTAMP,
    shr VARCHAR(80),
    shsj TIMESTAMP
);

CREATE TABLE hisbase (
    id VARCHAR(20) PRIMARY KEY,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    sid VARCHAR(20),
    jsnf VARCHAR(4),
    jsyf VARCHAR(2),
    jslb VARCHAR(20),
    bbz VARCHAR(20),
    hj2 INTEGER DEFAULT 0
);

CREATE TABLE hisbaseb (
    id VARCHAR(20) PRIMARY KEY,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    sid VARCHAR(20),
    jsnf VARCHAR(4),
    jsyf VARCHAR(2),
    jslb VARCHAR(20),
    bbz VARCHAR(20),
    hj2 INTEGER DEFAULT 0
);

CREATE TABLE dxl (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    xl VARCHAR(40),
    bbz VARCHAR(20),
    tjr VARCHAR(80),
    tjsj TIMESTAMP,
    shr VARCHAR(80),
    shsj TIMESTAMP
);

CREATE TABLE dxlb (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    dwbm VARCHAR(9) NOT NULL,
    grbm VARCHAR(5) NOT NULL,
    xl VARCHAR(40),
    bbz VARCHAR(20),
    tjr VARCHAR(80),
    tjsj TIMESTAMP,
    shr VARCHAR(80),
    shsj TIMESTAMP
);
