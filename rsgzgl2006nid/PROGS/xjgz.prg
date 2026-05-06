&&计算薪级工资
FUNCTION xjgz

PARAMETERS cXj

IF TYPE("cXj")<>"C"
   RETURN 00000
ENDIF

IF EMPTY(cXj)
    RETURN 00000
ENDIF
cXj=RIGHT("0"+ALLTRIM(cXj),2)

oldalias=ALIAS()
SELECT bz06_xjgz
LOCATE FOR xj=ALLTRIM(cXj)
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF
IF FOUND("bz06_xjgz")
*!*		IF !EMPTY(cDjc)
*!*			SELECT TOP 2 bz FROM bz06_xjgz WHERE tbnd=ALLTRIM(cTbnd) AND gwflbm=LEFT(cZwbm,2) ORDER BY bz DESC INTO ARRAY latxjbz
*!*			RETURN VAL(cDjc)*(latxjbz[1]-latxjbz[2])+bz06_xjgz.bz
*!*		ELSE
	    RETURN bz06_xjgz.bz
*!*		ENDIF
ELSE
    RETURN 00000
ENDIF
