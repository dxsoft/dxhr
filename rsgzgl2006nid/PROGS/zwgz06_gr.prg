&&计算职务工资
FUNCTION zwgz06_gr

PARAMETERS cZwbm,cZwdc,cDjc,cTbnd

LOCAL retv

IF TYPE("cZwbm")<>"C" OR TYPE("cZwdc")<>"C" OR TYPE("cTbnd")<>"C"
   RETURN 00000
ENDIF

IF ISNULL(cZwdc)
    RETURN 00000
ENDIF

IF EMPTY(cZwbm) OR EMPTY(cZwdc)
    RETURN 00000
ENDIF

oldalias=ALIAS()
SELECT bz06_zwgz_gr
LOCATE FOR tbnd=ALLTRIM(cTbnd) AND zwbm=ALLTRIM(cZwbm)
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF
IF FOUND("bz06_zwgz_gr")
   loc="bz06_zwgz_gr.dc"+ALLTRIM(cZwdc)
   IF !EMPTY(FIELD("dc"+ALLTRIM(cZwdc),"bz06_zwgz_gr"))
      retv = &loc
   ELSE
      retv = 00000
   ENDIF
ELSE
   retv = 00000
ENDIF

IF !EMPTY(cDjc)
   loc="bz06_zwgz_gr.dc"+ALLTRIM(STR(VAL(cZwdc)-1))
   IF !EMPTY(FIELD("dc"+ALLTRIM(cZwdc),"bz06_zwgz_gr"))
      retv = retv + retv - &loc
   ENDIF
ENDIF

RETURN retv