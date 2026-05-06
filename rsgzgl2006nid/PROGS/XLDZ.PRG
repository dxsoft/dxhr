
PARAMETERS dwsx,xl

SEEK ALLTRIM(dwsx)+ALLTRIM(xl) ORDER tag bmxl IN xldzbz

IF FOUND("xldzbz")
    RETURN xldzbz.zwbm+xldzbz.gzdc+xldzbz.gzjb
ELSE
    RETURN ""
ENDIF
