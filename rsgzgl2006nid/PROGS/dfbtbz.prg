FUNCTION dfbtbz

PARAMETERS tczwbm,tctbnd,tcjxlb

LOCAL  latdfbt
DIMENSION latdfbt[1]

IF LEFT(tczwbm,2)>'06' AND LEFT(tczwbm,2)<'20'
    SELECT bz FROM bz06_jbt WHERE UPPER(item)=="DFBT2" AND zwbm=tczwbm AND tbnd=tctbnd AND jxlb=tcjxlb INTO ARRAY latdfbt
ELSE
    SELECT bz FROM bz06_jbt WHERE UPPER(item)=="DFBT2" AND zwbm=tczwbm AND tbnd=tctbnd INTO ARRAY latdfbt
ENDIF

IF _tally>0
    RETURN latdfbt[1]
ELSE
    RETURN 0000
ENDIF

