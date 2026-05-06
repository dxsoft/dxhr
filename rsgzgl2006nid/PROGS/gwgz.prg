&&计算薪级工资
FUNCTION gwgz

PARAMETERS czwbm

IF TYPE("czwbm")<>"C"
   RETURN 00000
ENDIF

IF EMPTY(czwbm)
    RETURN 00000
ENDIF


oldalias=ALIAS()
SELECT bz06_zwgz
LOCATE FOR zwbm=ALLTRIM(czwbm)
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF
IF FOUND("bz06_zwgz")
    RETURN bz06_zwgz.bz
ELSE
    RETURN 00000
ENDIF
