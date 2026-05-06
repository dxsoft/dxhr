FUNCTION crtvgzbz1

PARAMETERS tcConn

IF USED("zwgzbz")
    USE IN zwgzbz
ENDIF
SQLEXEC(tcConn,"select * from zwgzbz","zwgzbz")

IF USED("jxjtbz")
    USE IN jxjtbz
ENDIF
SQLEXEC(tcConn,"select * from jxjtbz","jxjtbz")

*********

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

IF USED("bz06_xjgz")
    USE IN bz06_xjgz
ENDIF
SQLEXEC(tcConn,"select * from bz06_xjgz","bz06_xjgz")
