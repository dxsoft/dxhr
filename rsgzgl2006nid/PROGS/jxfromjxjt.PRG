FUNCTION jxfromjxjt

PARAMETERS tcTbnd,tcJxjt

IF tcJxjt<=0 OR EMPTY(tcTbnd)
    RETURN ""
ENDIF
oldalias=ALIAS()
SELECT jxjtbz
LOCATE FOR tbnd=tcTbnd AND jtbz=tcJxjt
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF
IF FOUND("jxjtbz")
    RETURN jxjtbz.jx
ELSE
    RETURN ""
ENDIF
