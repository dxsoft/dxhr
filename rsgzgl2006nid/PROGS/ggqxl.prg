FUNCTION ggqxl

PARAMETERS tcRybm
SELECT xl,bysj FROM xl WHERE dwbm+grbm=tcRybm AND xllb<>"ÆäËü" ORDER BY bysj INTO ARRAY laXL
IF _tally>0
	FOR i=1 TO ALEN(laXl,1)
	    IF laXl[i,2]>='1993.10'
	        EXIT
	    ENDIF
	ENDFOR
	IF i>=2
	    RETURN laXL[i-1,2]+laXl[i-1,1]
	ELSE
	    RETURN ""
	ENDIF
ELSE
    RETURN ""
ENDIF