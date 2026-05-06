&&计算薪级工资
FUNCTION djc_xj

PARAMETERS cDjc,cTbnd,cZwbm

IF TYPE("cXj")<>"C" OR TYPE("cTbnd")<>"C" OR TYPE("cZwbm")<>"C"
   RETURN 00000
ENDIF

IF EMPTY(cXj)
    RETURN 00000
ENDIF
SELECT TOP 2 bz FROM bz06_xjgz WHERE tbnd=ALLTRIM(cTbnd) AND gwflbm=LEFT(cZwbm,2) ORDER BY bz DESC INTO ARRAY latxjbz
RETURN latxjbz[1]-latxjbz[2]
