FUNCTION crtvgzbz

PARAMETERS tcConn

IF USED("jcglgzbz")
    USE IN jcglgzbz
ENDIF
SQLEXEC(tcConn,"select * from jcglgzbz","jcglgzbz")

IF USED("jsdjgzbz")
    USE IN jsdjgzbz
ENDIF
SQLEXEC(tcConn,"select * from jsdjgzbz","jsdjgzbz")
SELECT jsdjgzbz
INDEX ON tbnd+zwbm TAG ndbm ADDITIVE

IF USED("zwgzbz")
    USE IN zwgzbz
ENDIF
SQLEXEC(tcConn,"select * from zwgzbz","zwgzbz")

SELECT zwgzbz
INDEX ON tbnd+zwbm TAG ndbm ADDITIVE

IF USED("jxgzbz")
    USE IN jxgzbz
ENDIF
SQLEXEC(tcConn,"select * from jxgzbz","jxgzbz")

IF USED("jbgzbz")
    USE IN jbgzbz
ENDIF
SQLEXEC(tcConn,"select * from jbgzbz","jbgzbz")
SELECT jbgzbz
INDEX ON tbnd TAG tbnd ADDITIVE

IF USED("jbtbz")
    USE IN jbtbz
ENDIF
SQLEXEC(tcConn,"select * from jbtbz","jbtbz")
SELECT jbtbz
INDEX ON tbnd+bm TAG ndbm ADDITIVE 

*!*	IF USED("jxjtbz")
*!*	    USE IN jxjtbz
*!*	ENDIF
*!*	SQLEXEC(tcConn,"select * from jxjtbz","jxjtbz")
*!*	SELECT jxjtbz
*!*	INDEX ON tbnd+jx TAG NDJX ADDITIVE
*!*	INDEX ON lb+tbnd TAG TBND ADDITIVE

*********
IF USED("bz06_jbt")
    USE IN bz06_jbt
ENDIF

SQLEXEC(tcConn,"select * from bz06_jbt","bz06_jbt")

IF USED("bz06_jbgz")
    USE IN bz06_jbgz
ENDIF
SQLEXEC(tcConn,"select * from bz06_jbgz","bz06_jbgz")

IF USED("bz06_zwgz")
    USE IN bz06_zwgz
ENDIF
SQLEXEC(tcConn,"select * from bz06_zwgz","bz06_zwgz")

IF USED("bz06_zwgz_gr")
    USE IN bz06_zwgz_gr
ENDIF
SQLEXEC(tcConn,"select * from bz06_zwgz_gr","bz06_zwgz_gr")

IF USED("bz06_zwgz_fj")
    USE IN bz06_zwgz_fj
ENDIF
SQLEXEC(tcConn,"select * from bz06_zwgz_fj","bz06_zwgz_fj")

IF USED("bz06_djgz")
    USE IN bz06_djgz
ENDIF
SQLEXEC(tcConn,"select * from bz06_djgz","bz06_djgz")

IF USED("bz06_xjgz")
    USE IN bz06_xjgz
ENDIF
SQLEXEC(tcConn,"select * from bz06_xjgz","bz06_xjgz")

IF USED("bz06_blfb")
    USE IN bz06_blfb
ENDIF
SQLEXEC(tcConn,"select * from bz06_blfb","bz06_blfb")
SELECT bz06_blfb
INDEX ON zwbm TAG zwbm ADDITIVE

IF USED("bz06_zw_gw")
    USE IN bz06_zw_gw
ENDIF
SQLEXEC(tcConn,"select * from bz06_zw_gw","bz06_zw_gw")

IF USED("bz06_zzdz")
    USE IN bz06_zzdz
ENDIF
SQLEXEC(tcConn,"select * from bz06_zzdz","bz06_zzdz")

IF USED("njbt")
    USE IN njbt
ENDIF
SQLEXEC(tcConn,"select * from njbt","njbt")

IF USED("bz06_jjjy")
    USE IN bz06_jjjy
ENDIF
SQLEXEC(tcConn,"select * from bz06_jjjy","bz06_jjjy")

crtvjxjtbz(.f.,tcConn)
