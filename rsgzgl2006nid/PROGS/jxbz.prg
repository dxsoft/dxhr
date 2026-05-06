FUNCTION jxbz

PARAMETERS ctbnd,czwbm,cdwbm

IF ctbnd>='201607'
	SELECT bz FROM bz06_jbt WHERE tbnd=STRTRAN(ctbnd,".") AND UPPER(item)="DFBT2" AND jxlb=5 AND zwbm=czwbm INTO CURSOR curjx
ELSE
	SELECT bz FROM bz06_jbt WHERE tbnd=STRTRAN(ctbnd,".") AND UPPER(item)="DFBT2" AND jxlb=jxlb(cdwbm) AND zwbm=czwbm INTO CURSOR curjx
ENDIF

IF _tally>0
    RETURN curjx.bz
ELSE
    RETURN 0000
ENDIF
