FUNCTION crtvsrckhjg

PARAMETERS tcConn

IF USED("srckhjg")
    USE IN srckhjg
ENDIF
SQLEXEC(tcConn,"select * from srckhjg","srckhjg")
