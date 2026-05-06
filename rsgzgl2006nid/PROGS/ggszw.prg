FUNCTION ggszw

PARAMETERS tcRybm,tcGwflbm
SELECT srny,zwbm,xzzw FROM ryzwbh WHERE dwbm+grbm=tcRybm AND LEFT(zwbm,2)=tcGwflbm ORDER BY srny INTO ARRAY laZwbh
IF _tally=0
    RETURN ""
ENDIF

FOR i=1 TO ALEN(laZwbh,1)
    IF laZwbh[i,1]>='1993.10'
        EXIT
    ENDIF
ENDFOR

IF i>=2
    RETURN laZwbh[i-1,1]+laZwbh[i-1,2]+laZwbh[i-1,3]
ELSE
    RETURN ""
ENDIF
