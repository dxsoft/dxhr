FUNCTION fldcap

PARAMETERS tcFldName,tcdwsx

SEEK UPPER(tcFldName) ORDER tag fldname IN fldgz

IF FOUND("fldgz")
    IF INLIST(tcdwsx,"07","08","09","10")
        RETURN ALLTRIM(fldgz.field_caps)
    ELSE
        RETURN ALLTRIM(fldgz.field_cap)
    ENDIF
ELSE
    SEEK UPPER(tcFldName) ORDER tag fldname IN fldjbxx
    IF FOUND("fldjbxx")
*!*		    IF INLIST(tcdwsx,"07","08","09","10")
*!*		        RETURN ALLTRIM(fldjbxx.field_caps)
*!*		    ELSE
	        RETURN ALLTRIM(fldjbxx.field_cap)
*!*		    ENDIF
	ELSE
        RETURN SPACE(16)
    ENDIF
ENDIF
