FUNCTION zwbm

PARAMETERS tczwmc

IF EMPTY(tcZwmc)
    RETURN ""
ENDIF
IF !USED("dmb")
    crtvdmb(.f.,conn)
ENDIF

SEEK "001"+ALLTRIM(tczwmc) ORDER tag zwmc IN dmb
IF FOUND("dmb") AND LEN(ALLTRIM(tcZwmc))=LEN(ALLTRIM(dmb.mc))
    RETURN SUBSTR(dmb.bm,4,4)
ELSE
    RETURN ""
ENDIF
