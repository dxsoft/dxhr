FUNCTION XlA

PARAMETERS tcRybm,tcDate

SELECT xlbm,xl,STRTRAN(bysj,"."),xllb FROM xl WHERE dwbm+grbm=tcrybm AND xllb<>"ÆäËü" AND !EMPTY(STRTRAN(bysj,".")) ORDER BY bysj DESC,xlbm ASC INTO ARRAY lsarray 
IF _tally>0
    FOR i=1 TO ALEN(lsarray,1)
        IF lsarray[i,3]<=tcdate
            RETURN lsarray[i,1]+RIGHT(SPACE(6)+lsarray[i,3],6)+RIGHT(SPACE(18)+lsarray[i,2],18)+lsarray[i,4]
        ENDIF
    ENDFOR
ENDIF

RETURN SPACE(36)
