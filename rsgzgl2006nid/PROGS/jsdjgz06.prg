&&计算职务工资
FUNCTION jsdjgz06

PARAMETERS cZwbm,cTbnd

IF TYPE("cZwbm")<>"C" OR TYPE("cTbnd")<>"C"
   RETURN 00000
ENDIF

IF EMPTY(cZwbm)
    RETURN 00000
ENDIF

oldalias=ALIAS()
SELECT bz06_zwgz_gr
LOCATE FOR tbnd=ALLTRIM(cTbnd) AND zwbm=ALLTRIM(cZwbm)
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF
IF FOUND("bz06_zwgz_gr")
   RETURN bz06_zwgz_gr.jsdjgz
ELSE
   RETURN 00000
ENDIF
