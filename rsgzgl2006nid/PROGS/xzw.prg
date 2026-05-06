&&现任职务
FUNCTION xzw

PARAMETERS cRybm,cDwsx,cCurrentDate,cZzny,cCate

SELECT zwbm1,zwbm2,jsnf,jsyf FROM gwzh WHERE dwbm+grbm=cRybm INTO ARRAY lat
IF _tally>0 AND cCurrentDate<lat[1,3]+"."+lat[4]
    cDwsx=LEFT(lat[1],2)
ENDIF
RELEASE lat 

SELECT &cCate,srny FROM ryzwbh WHERE dwbm+grbm=cRybm AND LEFT(zwbm,2)=cDwsx AND srny<cCurrentDate ORDER BY srny DESC INTO ARRAY lsarray

IF _tally>0
   	RETURN ALLTRIM(lsarray[1,1])+SPACE(20)
ELSE
    IF !EMPTY(STRTRAN(cZzny,".")) AND cCurrentDate<cZzny
	    IF cCate="zwbm"
	        RETURN cDwsx+"FF"
	    ELSE
	        DO case
	        CASE cDwsx="07" 
	            RETURN "试用期职员"
	        CASE cDwsx="08" 
	            RETURN "事业学徒期技工"
	        CASE cDwsx="09" 
	            RETURN "事业学徒期普工"
	        CASE cDwsx="10" 
	            RETURN "见习期专业技术人员"
	        ENDCASE
	    ENDIF
	ELSE
        RETURN SPACE(20)
    ENDIF
ENDIF
