FUNCTION Jx

PARAMETERS tcRybm,tcDate

SELECT jx,STRTRAN(sysj,".") FROM jx WHERE dwbm+grbm=tcrybm ORDER BY sysj DESC INTO ARRAY lsarray 
IF _tally>0
    FOR i=1 TO ALEN(lsarray,1)
        IF lsarray[i,2]<=STRTRAN(tcdate,".","")
            RETURN lsarray[i,2]+lsarray[i,1]
        ENDIF
    ENDFOR
ENDIF

RETURN SPACE(36)
