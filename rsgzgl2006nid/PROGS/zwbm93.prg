FUNCTION zwbm93

PARAMETERS tcdwbm,tcgrbm,tcdwsx

SELECT zwbm,srny FROM ryzwbh WHERE dwbm=tcdwbm AND grbm=tcgrbm AND LEFT(zwbm,2)=tcdwsx ORDER BY srny INTO ARRAY lsarray
IF _tally>0
    FOR i=1 TO _tally
        IF lsarray[i,2]>"1993.09"
            EXIT
        ENDIF
    ENDFOR
    IF i=1
        RETURN ""
    ELSE
        RETURN lsarray[i-1,1]
    ENDIF
ELSE
    RETURN ""
ENDIF

        