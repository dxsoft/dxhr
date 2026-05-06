FUNCTION zStr

PARAMETERS nValue

IF nValue=0
    RETURN ""
ELSE
	IF INT(nValue)=nValue
	    RETURN ALLTRIM(STR(nValue))
	ELSE
		IF cyxx.blxs="0"
	        RETURN ALLTRIM(STR(nValue))
		ELSE
		    RETURN ALLTRIM(STR(nValue,7,1))
		ENDIF
	ENDIF
ENDIF
