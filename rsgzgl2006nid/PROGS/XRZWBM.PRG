FUNCTION xrzwbm

PARAMETERS tcRybm

SELECT zwbm FROM ryzwbh ORDER BY srny WHERE dwbm+grbm=tcRybm AND STRTRAN(srny,".","")<"200607" INTO ARRAY laXrzw
IF _tally>0
    RETURN laXrzw[ALEN(laXrzw,1)]
ELSE
    RETURN ""
ENDIF
