CREATE CURSOR cxjg (dwbm c(9),grbm c(5))

LOCAL j

SELECT ryjbxx
GO top
SCAN FOR LEFT(Zwbm2,2)<"05"
	IF INLIST(LEFT(Zwbm2,2),"01","02","03")
	    tcZwbm=LEFT(Zwbm2,3)+"0"
	ENDIF
	IF dryjbxx.dwbm="026" AND grbm="00075"
	    aaaaa=1
	ENDIF 
    m.tngl=INT(2008-VAL(LEFT(cjgzny,4))+1)+bjglxlnx-zdgznx-Kjtgnx(dwbm+grbm)
	m.v_rzjl=rzjlall(dwbm+grbm,dwsx)
	IF !EMPTY(m.v_rzjl)
	    &&查找当前职务
	    i=0
	    v_zwbm= LEFT(m.v_rzjl,4)
	    IF INLIST(LEFT(v_zwbm,2),"01","02","03")
	        v_zwbm=LEFT(v_zwbm,3)+"0"
	    ENDIF
	    IF tczwbm<>v_zwbm
	        i=5
	        DO WHILE .T.
	            v_zwbm=SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i)+1,AT(",",m.v_rzjl,i+1)-AT(",",m.v_rzjl,i)-1)
			    IF INLIST(LEFT(v_zwbm,2),"01","02","03")
			        v_zwbm=LEFT(v_zwbm,3)+"0"
			    ENDIF

	            IF !EMPTY(v_zwbm) AND tczwbm<>v_zwbm
	                i=i+5
	            ELSE
	                EXIT
	            ENDIF
	        ENDDO
	    ENDIF
	    IF tczwbm<>v_zwbm
	        i=0
	    ENDIF
	    &&查找当前职务

	    v_rznx = 2008-VAL(SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+2)+1,4))+1-VAL(SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+4)+1,AT(",",m.v_rzjl,i+5)-AT(",",m.v_rzjl,i+4)-1))

		oldalias=ALIAS()
		SELECT bz06_tgb
		LOCATE FOR zwbm=v_zwbm AND BETWEEN(v_rznx,rzns,rznz) AND BETWEEN(tngl,tgns,tgnz)
		IF FOUND("bz06_tgb") AND VAL(bz06_tgb.jb)>VAL(dryjbxx.jbgzjb2)&&现职务级别高于规定级别
		     INSERT INTO cxjg (dwbm,grbm) VALUES (dryjbxx.dwbm,dryjbxx.grbm)
		ENDIF
	ENDIF
ENDSCAN
