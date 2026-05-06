FUNCTION gwfl

PARAMETERS tcdwsx

oldalias=ALIAS()

SELECT dmb
LOCATE FOR bm="026"+ALLTRIM(tcdwsx) AND LEN(ALLTRIM(bm))=5
SELECT (oldalias)

IF FOUND("dmb")
    
    RETURN dmb.mc
ELSE
    RETURN ""
ENDIF
