FUNCTION cap

PARAMETERS tn,fn

LOCAL ali
ali=SELECT()

SELECT fldjbxx
LOCATE FOR UPPER(ALLTRIM(tblname))==UPPER(ALLTRIM(tn)) AND UPPER(ALLTRIM(field_name))==UPPER(ALLTRIM(fn))
SELECT (ali)
IF FOUND("fldjbxx")
    RETURN fldjbxx.field_cap
ELSE
    RETURN ""
ENDIF
