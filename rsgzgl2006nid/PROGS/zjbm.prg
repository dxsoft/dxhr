&&职务级别
FUNCTION zjbm

PARAMETERS cRybm,cDwsx,cCurrentDate

SELECT zjbm,xrzw,srny FROM ryzwbh WHERE dwbm+grbm=cRybm AND LEFT(zwbm,2)=cDwsx ORDER BY srny DESC INTO ARRAY lsarray

IF _tally>0
	m.tag=.F.
	FOR i=1 to ALEN(lsarray,1) 
	    IF cCurrentDate >= lsarray[i,3]
	        m.tag=.T.
	        EXIT
	    ENDIF
	ENDFOR

	IF m.tag
    	RETURN LEFT(RIGHT(SPACE(4)+ALLTRIM(lsarray[i,1]),4)+ALLTRIM(lsarray[i,2])+SPACE(20),20)
    ELSE
        RETURN SPACE(20)
    ENDIF
    
ELSE
    RETURN SPACE(20)
ENDIF
