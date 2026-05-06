FUNCTION txpd

PARAMETERS cxb,ccsny,czwbm,ccurdate

IF cxb="ÄÐ"
    IF ALLTRIM(STR(VAL(LEFT(ccsny,4))+60))+RIGHT(ccsny,2)<ccurdate
        RETURN 1
    ENDIF
ELSE
    IF INLIST(LEFT(czwbm,2),"05","06","08","09")
	    IF ALLTRIM(STR(VAL(LEFT(ccsny,4))+50))+RIGHT(ccsny,2)<ccurdate
		    RETURN 1
        ENDIF
    ELSE
	    IF ALLTRIM(STR(VAL(LEFT(ccsny,4))+55))+RIGHT(ccsny,2)<ccurdate
		    RETURN 1
        ENDIF
    ENDIF
ENDIF
RETURN 0