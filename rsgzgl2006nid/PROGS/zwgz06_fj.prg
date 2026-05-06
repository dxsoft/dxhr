&&计算职务工资
FUNCTION zwgz06_fj

PARAMETERS cZwbm,cZwdc,cTbnd

IF TYPE("cZwbm")<>"C" OR TYPE("cZwdc")<>"C" OR TYPE("cTbnd")<>"C"
   RETURN 0
ENDIF

IF EMPTY(cZwbm) OR EMPTY(cZwdc)
    RETURN 0
ENDIF

oldalias=ALIAS()
SELECT bz06_zwgz_fj
LOCATE FOR tbnd=ALLTRIM(cTbnd) AND zwbm=LEFT(ALLTRIM(cZwbm),2)+"0"+SUBSTR(czwbm,4)
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF
IF FOUND("bz06_zwgz_fj")
   loc="bz06_zwgz_fj.dc"+ALLTRIM(cZwdc)
   IF !EMPTY(FIELD("dc"+ALLTRIM(cZwdc),"bz06_zwgz_fj"))
      RETURN &loc
   ELSE
      RETURN 0
   ENDIF
ELSE
   RETURN 0
ENDIF
