FUNCTION jcjx

&&计算基础绩效

PARAMETERS tcdwbm,tczwbm,tcbz

LOCAL  latdfbt,v_bl,ajxbl,lnBz

IF LEFT(tczwbm,2)="03"
    tczwbm="01"+STRTRAN(STRTRAN(STRTRAN(SUBSTR(tczwbm,4,1),"B","A"),"C","B"),"D","B")+"0"
ENDIF

IF LEFT(tczwbm,2)='04'
	tczwbm="01"+SUBSTR(tczwbm,4,2)
ENDIF

IF AT("F",tczwbm)>0 AND LEFT(tcbz,4)<='2009'&&试用期，2009年前津补贴标准不分学历
    tczwbm=LEFT(tczwbm,3)+"F"
ENDIF

DIMENSION latdfbt[1]
DIMENSION ajxbl[1]

SELECT jxbl,jxlb FROM dwbm WHERE dwbm=tcdwbm INTO ARRAY ajxbl

IF _tally>0
	IF tcbz>='201410' AND INLIST(LEFT(tczwbm,2),"07","08","09","10","11")&&201410前区分绩效类别
		SELECT bz FROM bz06_jbt WHERE UPPER(item)=="DFBT2" AND zwbm=tczwbm AND tbnd=tcbz AND jxlb=5 INTO ARRAY latdfbt
	ELSE
	    IF INLIST(LEFT(tczwbm,2),"07","08","09","10","11")
		    SELECT bz FROM bz06_jbt WHERE UPPER(item)=="DFBT2" AND zwbm=tczwbm AND tbnd=tcbz AND jxlb=ajxbl[1,2] INTO ARRAY latdfbt
		ELSE
		    SELECT bz FROM bz06_jbt WHERE UPPER(item)=="DFBT2" AND zwbm=tczwbm AND tbnd=tcbz AND jxlb=1 INTO ARRAY latdfbt
        ENDIF
	ENDIF
		
	IF _tally>0
	    m.lnBz=latdfbt[1]
	ELSE
	    m.lnBz=0000
	ENDIF

	IF AT("F",tczwbm)>0&&试用期、见习期不折算
		RETURN m.lnBz
	ENDIF
		
	IF INLIST(LEFT(tczwbm,2),'01','02','03','04','05','06','21','22','23','24','25','26','27','28','29')&&公务员
	    RETURN m.lnBz
	ELSE
		IF EMPTY(ajxbl[1,1])
		    RETURN 0000
		ELSE
		    v_bl=STRTRAN(ajxbl[1,1],"：",":")
		ENDIF
		RETURN m.lnBz*10*VAL(LEFT(v_bl,AT(":",v_bl)-1))/(VAL(LEFT(v_bl,AT(":",v_bl)-1))+VAL(SUBSTR(v_bl,AT(":",v_bl)+1)))/7
	ENDIF
	
ELSE
    RETURN 0000
ENDIF

