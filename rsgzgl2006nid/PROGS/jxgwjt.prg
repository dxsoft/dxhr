FUNCTION jxgwjt

PARAMETERS tcTbnd,tcDwbm,tcZwbm

IF EMPTY(tcDwbm) OR EMPTY(tcTbnd) OR EMPTY(tcZwbm)
    RETURN 0
ENDIF

SELECT bz FROM bz06_jbt WHERE zwbm=tczwbm AND jxlb IN (SELECT jxlb FROM dwbm WHERE dwbm=tcdwbm) AND tbnd=tcTbnd INTO ARRAY latt
IF _tally>0
    RETURN latt[1]
ENDIF
RETURN 000
