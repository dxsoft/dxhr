FUNCTION zwjb
PARAMETERS tczwbm

IF tcZwbm>"1000"
    tczwbm="10"+RIGHT(tczwbm,2)
ENDIF

oldalias=ALIAS()

SELECT dmb
LOCATE FOR bm="026"+tczwbm AND LEN(ALLTRIM(bm))=7
SELECT (oldalias)

IF FOUND("dmb")
    RETURN ALLTRIM(dmb.mc)
ELSE
    RETURN ""
ENDIF
