FUNCTION crtvbz

PARAMETERS tcConn

IF USED("jbtgbz")
    USE IN jbtgbz
ENDIF
SQLEXEC(tcConn,"select * from jbtgbz","jbtgbz")

IF USED("xldzbz")
    USE IN xldzbz
ENDIF
SQLEXEC(tcConn,"select * from xldzbz","xldzbz")

IF USED("tgjjjy")
    USE IN tgjjjy
ENDIF
SQLEXEC(tcConn,"select * from tgjjjy","tgjjjy")

IF USED("bz06_zw_jb_xj")
    USE IN bz06_zw_jb_xj
ENDIF
SQLEXEC(tcConn,"select * from bz06_zw_jb_xj","bz06_zw_jb_xj")
SELECT bz06_zw_jb_xj
INDEX ON zwbm TAG zwbm ADDITIVE

IF USED("bz06_jjjy")
    USE IN bz06_jjjy
ENDIF

SQLEXEC(tcConn,"select * from bz06_jjjy","bz06_jjjy")

IF USED("bz06_zw_gw")
    USE IN bz06_zw_gw
ENDIF
SQLEXEC(tcConn,"select * from bz06_zw_gw","bz06_zw_gw")

IF USED("bz06_tgb")
    USE IN bz06_tgb
ENDIF
SQLEXEC(tcConn,"select * from bz06_tgb","bz06_tgb")

IF USED("bz06_zzdz")
    USE IN bz06_zzdz
ENDIF
SQLEXEC(tcConn,"select * from bz06_zzdz","bz06_zzdz")

IF USED("bz06_fjtgb")
    USE IN bz06_fjtgb
ENDIF
SQLEXEC(tcConn,"select * from bz06_fjtgb","bz06_fjtgb")

IF USED("bz_pskhj")
    USE IN bz_pskhj
ENDIF
SQLEXEC(tcConn,"select * from bz_pskhj","bz_pskhj")

IF USED("bz_wybt")
    USE IN bz_wybt
ENDIF
SQLEXEC(tcConn,"select * from bz_wybt","bz_wybt")

IF USED("bz_txbt")
    USE IN bz_txbt
ENDIF
SQLEXEC(tcConn,"select * from bz_txbt","bz_txbt")

IF USED("bz_wmj")
    USE IN bz_wmj
ENDIF
SQLEXEC(tcConn,"select * from bz_wmj","bz_wmj")