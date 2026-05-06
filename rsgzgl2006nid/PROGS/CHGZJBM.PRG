FUNCTION chgzjbm

PARAMETERS tczwmc

IF EMPTY(tcZwmc)
    RETURN ""
ENDIF
IF !USED("dmb")
    USE dmb IN 0
ENDIF

SEEK "026"+ALLTRIM(tczwmc) ORDER tag zwmc IN dmb
IF FOUND("dmb")
    RETURN SUBSTR(dmb.bm,4,4)
ELSE
    RETURN ""
ENDIF
