PARAMETERS tcTbnd,tcJx

IF EMPTY(tcJx) OR EMPTY(tcTbnd)
    RETURN 0
ENDIF

SEEK "sp"+tcTbnd+ALLTRIM(tcJx) ORDER tag ndjx IN jxjtbz

IF FOUND("jxjtbz")
    RETURN jxjtbz.jtbz
ELSE
    RETURN 0
ENDIF
