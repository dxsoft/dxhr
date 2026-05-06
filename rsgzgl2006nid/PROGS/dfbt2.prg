FUNCTION dfbt2

PARAMETERS tcdwbm,tczwbm,tcbz

LOCAL  latdfbt
DIMENSION latdfbt[1]

SELECT bz FROM bz06_jbt WHERE UPPER(item)=="DFBT2" AND zwbm=tczwbm AND tbnd=tcbz AND jxlb=(select jxlb FROM dwbm WHERE dwbm=tcdwbm) INTO ARRAY latdfbt
IF _tally>0
    RETURN latdfbt[1]
ELSE
    RETURN 0
ENDIF

