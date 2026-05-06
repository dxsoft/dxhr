FUNCTION jxgz06

PARAMETERS tcTbnd,tczwbm,tcXl

IF AT("F",tcZwbm)=0
    RETURN 0
ENDIF
IF EMPTY(tcXl)
    RETURN 0
ENDIF

LOCAL oldalias

oldalias=ALIAS()

SELECT bz06_zzdz 

IF INLIST(LEFT(tczwbm,2),'01','02','21','22','23','24','25','26','27','28')
    LOCATE FOR tbnd=tcTbnd AND LEFT(zzzwbm,2)='01' AND xlmc=ALLTRIM(tcXl)
ELSE
	IF tczwbm>"10"
	    tczwbm="10"
	ENDIF
    LOCATE FOR tbnd=tcTbnd AND LEFT(zzzwbm,2)=LEFT(tczwbm,2) AND xlmc=ALLTRIM(tcXl)
ENDIF

IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF

IF FOUND("bz06_zzdz")
*!*	    IF VAL(STRTRAN(m.rq,".","")) - VAL(STRTRAN(tcCjgzny,".",""))>=100
*!*	        IF bz06_zzdz.gz2>0
*!*	            RETURN bz06_zzdz.gz2
*!*	        ELSE
*!*	            RETURN bz06_zzdz.gz1
*!*	        ENDIF    
*!*	    ELSE
        RETURN bz06_zzdz.gz1
*!*	    ENDIF
ENDIF

RETURN 0
