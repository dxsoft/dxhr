FUNCTION jydjgz06&&警员等级工资

PARAMETERS tcJb,tcDc,tcTbnd

IF TYPE("tcJb")<>"C" OR TYPE("tcDc")<>"C" OR TYPE("tcTbnd")<>"C"
   RETURN 00000
ENDIF

IF EMPTY(tcJb) OR EMPTY(tcDc)
    RETURN 00000
ENDIF

oldalias=ALIAS()

SELECT BZ06_djgz
LOCATE FOR tbnd=tcTbnd AND VAL(jb)=VAL(tcJb)
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF

IF !EOF("bz06_djgz")
    IF !EMPTY(FIELD("Dc"+ALLTRIM(tcDc),"BZ06_djgz"))
	    IF VAL(zgdc(tcjb))<VAL(tcdc)&&倒档差
	        loc="bz06_djgz.Dc"+ALLTRIM(zgdc(tcjb))
	        loc1="bz06_djgz.Dc"+ALLTRIM(STR(VAL(zgdc(tcjb))-1))
	        RETURN CAST(&loc + (&loc - &loc1)*(VAL(tcdc)-VAL(zgdc(tcjb))) as i)
	    ELSE
	        loc="bz06_djgz.Dc"+ALLTRIM(tcDc)
            RETURN &loc
	    ENDIF
    ELSE
       RETURN 00000
    ENDIF
ELSE
   RETURN 00000
ENDIF
