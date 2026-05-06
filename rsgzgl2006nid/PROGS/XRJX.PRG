PARAMETERS tcRybm

SELECT jx,sysj FROM jx ORDER BY sysj DESC WHERE dwbm+grbm=tcRybm INTO ARRAY lsarray
IF _tally>0
    RETURN lsarray[1,2]+lsarray[1,1]
ELSE
    RETURN ""
ENDIF
