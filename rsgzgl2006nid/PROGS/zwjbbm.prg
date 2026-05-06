FUNCTION zwjbbm
PARAMETERS tczwmc

IF !USED("dmb")
    crtvdmb(.f.,conn)
ENDIF

oldalias=SELECT()

SELECT dmb
LOCATE FOR bm="026" AND mc=ALLTRIM(tczwmc) AND LEN(ALLTRIM(bm))=7
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF

IF FOUND("dmb")
    RETURN SUBSTR(ALLTRIM(dmb.bm),4,4)
ELSE
    RETURN ""
ENDIF
