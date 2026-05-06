FUNCTION fjtg

PARAMETERS tcjb,tcdc,tczwbm

*!*	csql="SELECT d"+LEFT(tczwbm,2)+SUBSTR(tczwbm,4,1)+" FROM bz06_fjtgb WHERE alltr(jb"+ALLTRIM(tcjb)+")=='"+ALLTRIM(tcdc)+"' into array lat"

csql="SELECT d03"+SUBSTR(tczwbm,4,1)+" FROM bz06_fjtgb WHERE alltr(jb"+ALLTRIM(tcjb)+")=='"+ALLTRIM(tcdc)+"' into array lat"

&csql
IF _tally>0
	RETURN ALLTRIM(lat[1])
ELSE
    RETURN ""
ENDIF
