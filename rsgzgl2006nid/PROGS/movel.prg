FUNCTION movel
PARAMETERS tctblname

LOCAL oldalias
oldalias=ALIAS()
SELECT (tctblname)
SELECT field_name FROM fldgz INTO ARRAY laFldName1
v_reccnt=_tally
FOR i=1 TO v_reccnt
    lcFldname=ALLTRIM(STRTRAN(laFldName1[i,1],"2",""))+"1"
    REPLACE &lcFldname WITH &laFldName1[i,1]
    lcFldname=ALLTRIM(laFldName1[i,1])
    IF TYPE(lcFldname)="N"
        REPLACE &lcFldname WITH 0
    ELSE
        REPLACE &lcFldname WITH ""
    ENDIF
ENDFOR
RELEASE laFldname1
REPLACE zwbm1 WITH zwbm2

IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF
RETURN 0