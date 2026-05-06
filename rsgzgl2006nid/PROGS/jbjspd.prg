tnjsnd=2008

CREATE CURSOR lst(dwbm c(9),grbm c(5))
SELECT dtgxx
SCAN
IF dwbm='001' AND grbm='00098'
aaaa=1
endif

	IF LEFT(dtgxx.Zwbm,2)>="05"
	    LOOP
	ENDIF

	IF dtgxx.zwbm="01FF" OR EMPTY(dtgxx.zwbm)
	    LOOP
	ENDIF

	IF INLIST(LEFT(dtgxx.Zwbm,2),"01","02","03")
	    tcZwbm=LEFT(dtgxx.Zwbm,3)+"0"
	ENDIF

	IF !EMPTY(dtgxx.zwbm1) AND INLIST(LEFT(dtgxx.Zwbm1,2),"01","02","03")
	    tcZwbm1=LEFT(dtgxx.Zwbm1,3)+"0"
	ELSE
	    tcZwbm1=''
	ENDIF

	v_rznx = rznx+tnjsnd-2006
	v_rznx1 = rznx1+tnjsnd-2006
	v_tgnx=tgnx+tnjsnd-2006
	v_tgjb=dtgxx.tgjb
	
	SELECT bz06_tgb
	LOCATE FOR zwbm=tczwbm AND BETWEEN(v_rznx,rzns,rznz) AND BETWEEN(v_tgnx,tgns,tgnz)
	IF FOUND("bz06_tgb") AND VAL(bz06_tgb.jb)<VAL(v_tgjb)
	    INSERT INTO lst (dwbm,grbm) VALUES (dtgxx.dwbm,dtgxx.grbm)
	ELSE
		IF !EMPTY(tczwbm1)
	        LOCATE FOR zwbm=tczwbm1 and BETWEEN(v_rznx1,rzns,rznz) AND BETWEEN(v_tgnx,tgns,tgnz)	    
			IF FOUND("bz06_tgb") AND VAL(bz06_tgb.jb)<=VAL(v_tgjb)&&原任低一职务达到套改级别规定年限
	            INSERT INTO lst (dwbm,grbm) VALUES (dtgxx.dwbm,dtgxx.grbm)
			ENDIF
		ENDIF
	ENDIF

    SELECT dtgxx
ENDSCAN