&&计算绩效工资
FUNCTION jcjxbz

PARAMETERS cZwbm,cTbnd,cXl,cJxlb

LOCAL lat
DIMENSION lat[1]

IF TYPE("cZwbm")<>"C" OR TYPE("cTbnd")<>"C"
   RETURN "      "
ENDIF

IF EMPTY(cZwbm)
    RETURN "      "
ENDIF

oldalias=SELECT()
SELECT bz06_jbt

IF UPPER(SUBSTR(czwbm,3,1))="F"
*!*	    SELECT bz FROM bz06_jbt WHERE UPPER(item)="DFBT2" AND tbnd=ALLTRIM(cTbnd) AND LEFT(zwbm,3)=LEFT(czwbm,3) AND jxlb=cjxlb INTO ARRAY lat
*!*	    IF _tally>0
*!*	        RETURN ALLTRIM(STR(lat[1]))+"－"+ALLTRIM(STR(lat[ALEN(lat,1)]))
*!*	    ELSE
*!*	        RETURN "      "
*!*	    ENDIF

    SELECT bz FROM bz06_jbt WHERE UPPER(item)="DFBT2" AND tbnd=ALLTRIM(cTbnd) AND LEFT(zwbm,3)=LEFT(czwbm,3) AND jxlb=cjxlb INTO ARRAY lat
    IF _tally>0
        RETURN ALLTRIM(STR(lat[1]))+"－"+ALLTRIM(STR(lat[ALEN(lat,1)]))
    ELSE
        RETURN "      "
    ENDIF
ELSE
    SELECT bz FROM bz06_jbt WHERE UPPER(item)="DFBT2" AND tbnd=ALLTRIM(cTbnd) AND zwbm=ALLTRIM(cZwbm) AND jxlb=cjxlb ORDER BY zwbm INTO ARRAY lat 
    IF _tally>0
        RETURN ALLTRIM(STR(lat[1]))
    ELSE
        RETURN "      "
    ENDIF
ENDIF
