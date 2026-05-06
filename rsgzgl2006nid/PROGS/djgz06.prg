FUNCTION djgz06&&级别工资

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
    IF !EMPTY(FIELD("Dc"+ALLTRIM(tcDc),"BZ06_JBGZ"))
        m.zgdc=zgdc(ALLTRIM(STR(VAL(tcjb)+7)))
	    IF VAL(m.zgdc)<VAL(tcdc)&&倒档差
	        loc="bz06_djgz.Dc"+ALLTRIM(m.zgdc)
	        loc1="bz06_djgz.Dc"+ALLTRIM(STR(VAL(m.zgdc)-1))
	        RETURN CAST(&loc + (&loc - &loc1)*(VAL(tcdc)-VAL(m.zgdc)) as i)
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
