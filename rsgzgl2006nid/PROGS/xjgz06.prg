&&计算薪级工资
FUNCTION xjgz06

PARAMETERS cXj,cDjc,cTbnd,cZwbm

IF TYPE("cXj")<>"C" OR TYPE("cDjc")<>"C" OR TYPE("cTbnd")<>"C" OR TYPE("cZwbm")<>"C"
   RETURN 00000
ENDIF

IF EMPTY(cXj)
    RETURN 00000
ENDIF
cXj=RIGHT("0"+ALLTRIM(cXj),2)

oldalias=ALIAS()
SELECT bz06_xjgz
LOCATE FOR tbnd=ALLTRIM(cTbnd) AND xj=ALLTRIM(cXj) AND gwflbm=LEFT(cZwbm,2)
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF
IF FOUND("bz06_xjgz")
	IF !EMPTY(cDjc)
		SELECT DISTINCT bz FROM bz06_xjgz WHERE tbnd=ALLTRIM(cTbnd) AND gwflbm=LEFT(cZwbm,2) ORDER BY bz DESC INTO ARRAY latxjbz
		RETURN VAL(cDjc)*(latxjbz[1]-latxjbz[2])+bz06_xjgz.bz
	ELSE
	    RETURN bz06_xjgz.bz
	ENDIF
ELSE
    RETURN 00000
ENDIF
