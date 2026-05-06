PARAMETERS tcTbnd,tcJx

IF EMPTY(tcJx) OR EMPTY(tcTbnd)
    RETURN 0
ENDIF

*!*	SEEK "jx"+tcTbnd+tcJx ORDER tag ndjx IN jxjtbz

SEEK tcTbnd+tcJx ORDER tag ndjx IN jxjtbz

IF FOUND("jxjtbz")
    RETURN jxjtbz.jtbz
ELSE
    RETURN 0000
ENDIF
