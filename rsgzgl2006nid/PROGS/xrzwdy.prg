&&现任职务名称
FUNCTION xrzwdy

PARAMETERS cRybm,cDwsx,cCurrentDate

SELECT xzzw,srny FROM dryzwbh WHERE dwbm+grbm=cRybm AND LEFT(zwbm,2)=cDwsx ORDER BY srny DESC INTO ARRAY lsarray

IF _tally>0
	FOR i=1 to ALEN(lsarray,1) 
	    IF cCurrentDate >= lsarray[i,2]
	        EXIT
	    ENDIF
	ENDFOR
    IF i<=ALEN(lsarray,1)
    	RETURN ALLTRIM(lsarray[i,1])
    ELSE
        RETURN ""
    ENDIF
ELSE
    RETURN ""
ENDIF
