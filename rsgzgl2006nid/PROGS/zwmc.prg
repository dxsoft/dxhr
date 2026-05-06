FUNCTION zwmc
PARAMETERS tczwbm

IF EMPTY(tczwbm)
    RETURN ""
ENDIF
IF tcZwbm>"1000" AND tcZwbm<"2000"
    tczwbm="10"+RIGHT(tczwbm,2)
ENDIF
IF tcZwbm="01B1"
    tczwbm="01B0"
ENDIF

IF AT("F",tcZwbm)>0
    tczwbm=LEFT(tczwbm,3)+"F"
ENDIF

oldalias=ALIAS()
IF !USED("dmb")
    crtvdmb(.f.,conn)
ENDIF
SELECT dmb
LOCATE FOR bm="051"+tczwbm AND LEN(ALLTRIM(bm))=7
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF

IF FOUND("dmb")
    RETURN dmb.mc
ELSE
    RETURN SPACE(18)
ENDIF
