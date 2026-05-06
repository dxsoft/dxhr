FUNCTION zjbm2

PARAMETERS tczwmc

IF EMPTY(tcZwmc)
    RETURN ""
ENDIF

oldalias=ALIAS()
SELECT dmb
LOCATE FOR ALLTRIM(mc)=ALLTRIM(tczwmc) AND LEFT(bm,3)="026" AND LEN(ALLTRIM(bm))>5
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF

IF FOUND("dmb")
    RETURN SUBSTR(dmb.bm,4,4)
ELSE
    RETURN ""
ENDIF
