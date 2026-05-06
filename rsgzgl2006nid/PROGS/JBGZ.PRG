FUNCTION jbgz&&级别工资

PARAMETERS tnJb,tcDc,tcTbnd

IF TYPE("tnJb")<>"N" OR TYPE("tcDc")<>"C" OR TYPE("tcTbnd")<>"C"
   RETURN 0
ENDIF

IF EMPTY(tnJb) OR EMPTY(tcDc)
    RETURN 0
ENDIF

IF VAL(tcDc)>14
    RETURN 0
ENDIF

oldalias=ALIAS()
SELECT jbgzbz
LOCATE FOR tbnd=tcTbnd AND jb=tnJb
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF

IF !EOF("jbgzbz")
   loc="jbgzbz.A"+ALLTRIM(tcDc)
   RETURN &loc
ELSE
   RETURN 0
ENDIF
