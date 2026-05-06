FUNCTION txsj

PARAMETERS cxb,ccsny,czwbm,nycsj

IF cxb="ÄÐ"
    RETURN ALLTRIM(STR(VAL(LEFT(ccsny,4))+60))+RIGHT(ccsny,3)
ELSE
    IF INLIST(LEFT(czwbm,2),"05","06","08","09")
	    RETURN ALLTRIM(STR(VAL(LEFT(ccsny,4))+50))+RIGHT(ccsny,3)
    ELSE
	    RETURN ALLTRIM(STR(VAL(LEFT(ccsny,4))+55+nycsj))+RIGHT(ccsny,3)
    ENDIF
ENDIF
RETURN ""