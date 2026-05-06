FUNCTION crtvsjwj

PARAMETERS tcConn

IF USED("sjwj")
    USE IN sjwj
ENDIF
SQLEXEC(tcConn,"select * from sjwj","sjwj")
