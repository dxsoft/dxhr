FUNCTION jxsysj

PARAMETERS tcrybm,tcjx

SEEK tcrybm+tcjx ORDER tag bmjx IN jx
IF FOUND("jx")
    RETURN jx.sysj
ELSE
    RETURN ""
ENDIF