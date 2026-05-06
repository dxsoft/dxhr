&&计算绩效工资
FUNCTION jcjxgz

PARAMETERS cZwbm,cTbnd,cXl,cJxlb

IF TYPE("cZwbm")<>"C" OR TYPE("cTbnd")<>"C"
   RETURN 0000
ENDIF

IF EMPTY(cZwbm)
    RETURN 0000
ENDIF

oldalias=SELECT()
SELECT bz06_jbt

IF UPPER(SUBSTR(czwbm,3,1))="F"
    LOCATE FOR UPPER(item)="DFBT2" AND tbnd=ALLTRIM(cTbnd) AND (zwbm=LEFT(czwbm,3)+xlcc(cXl) OR zwbm=ALLTRIM(cZwbm) OR EMPTY(zwbm)) AND jxlb=cjxlb
ELSE
    LOCATE FOR UPPER(item)="DFBT2" AND tbnd=ALLTRIM(cTbnd) AND zwbm=ALLTRIM(cZwbm) AND jxlb=cjxlb
ENDIF

SELECT (oldalias)

IF FOUND("bz06_jbt")
    RETURN bz06_jbt.bz
ELSE
    RETURN 0000
ENDIF
