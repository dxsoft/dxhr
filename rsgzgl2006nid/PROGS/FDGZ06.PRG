&&计算浮动工资
FUNCTION fdgz06

PARAMETERS cTbnd,cZwbm,cZwgzdc,cFddc

IF TYPE("cZwbm")<>"C" OR TYPE("cTbnd")<>"C" OR TYPE("cZwgzdc")<>"C" OR TYPE("cFddc")<>"C"
   RETURN 0
ENDIF

IF  EMPTY(cTbnd) OR EMPTY(cZwbm) OR EMPTY(cZwgzdc) OR EMPTY(cFddc)
    RETURN 0
ENDIF

IF VAL(cFddc)=0
    RETURN 0
ENDIF

oldalias=ALIAS()

IF USED("bz06_xjgz")
    SELECT bz06_xjgz
ELSE
    SELECT 0
    USE bz06_xjgz
ENDIF

LOCATE FOR tbnd=cTbnd AND gwflbm=LEFT(cZwbm,2) AND xj=RIGHT("0"+ALLTRIM(cZwgzdc),2)
IF !FOUND("bz06_xjgz")
	IF !EMPTY(oldalias)
	    SELECT (oldalias)
	ENDIF
    RETURN 0
ENDIF
m.xjgz1=bz06_xjgz.bz
LOCATE FOR tbnd=cTbnd AND gwflbm=LEFT(cZwbm,2) AND xj=RIGHT("0"+ALLTRIM(STR(VAL(cZwgzdc)+VAL(cFddc))),2)
IF !FOUND("bz06_xjgz")
	IF !EMPTY(oldalias)
	    SELECT (oldalias)
	ENDIF
    RETURN 0
ENDIF

m.xjgz2=bz06_xjgz.bz

IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF

RETURN m.xjgz2-m.xjgz1
