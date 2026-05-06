FUNCTION njbt

PARAMETERS tctbnd,tilb

LOCAL liNjbt,lcLb
DIMENSION liNjbt[1]

lcLb=ALLTRIM(STR(tiLb))

SELECT a&lclb FROM njbt WHERE tbnd=tctbnd INTO ARRAY liNjbt

IF _tally>0
    RETURN liNjbt[1]
ELSE
    RETURN 0
ENDIF
