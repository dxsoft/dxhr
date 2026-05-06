FUNCTION tgzwgw

PARAMETERS tczwbm

oldalias=ALIAS()
SELECT bz06_zw_gw
LOCATE FOR zwbm=IIF(LEFT(tczwbm,2)>"10","10"+RIGHT(tczwbm,2),tczwbm) AND tj1="1"
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF
IF !FOUND("bz06_zw_gw")
    RETURN SPACE(20)
ENDIF
RETURN RIGHT(SPACE(4)+bz06_zw_gw.gwbm,4)+LEFT(bz06_zw_gw.gwmc+SPACE(20),20)