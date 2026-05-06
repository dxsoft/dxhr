FUNCTION rqpd

PARAMETERS v_ny

IF EMPTY(STRTRAN(v_ny,'.'))&&Ã»Â¼Èë
    RETURN .T.
ENDIF

v_xsdwz=AT('.',v_ny,1)
v_nf=LEFT(v_ny,v_xsdwz-1)
IF LEN(ALLTRIM(v_nf)) < 4 OR VAL(v_nf)>m.pnYear
    RETURN .f.
ENDIF

v_yf=ALLTRIM(SUBSTR(v_ny,v_xsdwz+1,2))
IF LEN(v_yf)<2 or v_yf>'12' or v_yf<'01'
    RETURN .f.
ENDIF
RETURN .t.
