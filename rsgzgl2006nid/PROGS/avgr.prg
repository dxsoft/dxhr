FUNCTION avgr

PARAMETERS tdwbm,tgrbm,tnd

*!*	IF tgrbm="00071"
*!*	aaaa=1
*!*	ENDIF

LOCAL jbgz,jt,ys,ze,ki,retv,y1,m.start,m.zwbm,m.latgz

DIMENSION latgz[1]

m.ys=0
m.ze=0
m.jbgz=0
m.jt=0
m.retv=""
m.y1=0
m.start=0
m.zwbm=""

&&公务员和见习人员不折算dfbt,标准按信息中执行标准
*!*	SELECT dwbm,grbm,zwbm2,zwgzdc2,jbgzjb2,zwgzse2+jbgzse2+jsfszwtg2+jsdjgz2+fdgz2+jjjy2+blfb2+jhljt+qtbt+jxgz+tgblbf as jbgz,IIF(LEFT(zwbm2,2)<"07" OR AT("F",zwbm2)>0,dfbt2,int(jxbz(STRTRAN(jbtbz,"."),zwbm2)*10/7+0.5))+IIF(AT("警",jx)>0,jxjt,0) as jt,jsnf,jsyf FROM hisbase WHERE dwbm=tdwbm AND grbm=tgrbm ORDER BY jsnf,jsyf,hj2 INTO ARRAY latgz

&&公务员和见习人员不折算dfbt，标准按时间自动计算（考虑没调津补贴的情况）
SELECT dwbm,grbm,zwbm2,zwgzdc2,jbgzjb2,zwgzse2+jbgzse2+jsfszwtg2+jsdjgz2+fdgz2+jjjy2+blfb2+jhljt+qtbt+jxgz+gwjt2+tgblbf+njbt as jbgz,IIF(LEFT(zwbm2,2)<"07" OR AT("F",zwbm2)>0,dfbt2,int(jxbz(tbnd(jsnf+jsyf,"bz06_jbt"),zwbm2,dwbm)*10/7+0.5))+IIF(AT("警",jx)>0,jxjt,0) as jt,jsnf,jsyf FROM hisbase WHERE dwbm=tdwbm AND grbm=tgrbm AND bbz<>'模拟推算' ORDER BY jsnf,jsyf,hj2 INTO ARRAY latgz

IF _tally>0
    &&2013
    tnd="2013"
	m.jbgz=latgz[1,6]
	m.jt=latgz[1,7]
    IF latgz[1,8]>"2013"&&2014后进入
        m.ze=0
    ELSE
	    m.start=VAL(latgz[1,9])
	        	
		FOR ki=1 TO ALEN(latgz,1)
		    IF latgz[ki,8]+latgz[ki,9]>tnd+"01" AND latgz[ki,8]=tnd
	   	        m.ze=m.ze+(m.jbgz+m.jt)*(VAL(latgz[ki,9])-m.start)&&绩效折算成100%
		        m.y1=m.y1+VAL(latgz[ki,9])-m.start
	            m.start=VAL(latgz[ki,9])
		    ELSE
		        IF latgz[ki,8]>tnd
		            EXIT
		        ENDIF
        	    m.start=1
		    ENDIF
		    m.jt=latgz[ki,7]
		    m.jbgz=latgz[ki,6]
		    m.zwbm=latgz[ki,3]
		ENDFOR
	    m.ze=m.ze+(m.jbgz+m.jt)*(13-m.start)
	    m.y1=m.y1+(13-m.start)
		IF LEFT(m.zwbm,2)<"07" AND AT("F",m.zwbm)<=0 AND INLIST(dkhjg(dwbm+grbm,"2013"),'优秀','合格','称职')
		    m.ze=m.ze+m.jbgz
		ENDIF
    ENDIF
   
	IF m.ze>0
	    m.retv=STR(INT(m.ze/y1+0.5))
	ELSE
	    m.retv="0000"
	ENDIF

    &&2014
    m.ys=0
    m.ze=0
    tnd="2014"
	m.jbgz=latgz[1,6]
	m.jt=latgz[1,7]
	m.y1=0
    IF latgz[1,8]>"2014"&&2015后进入
        m.ze=0
    ELSE
	    m.start=VAL(latgz[1,9])
	        	
		FOR ki=1 TO ALEN(latgz,1)
		    IF latgz[ki,8]+latgz[ki,9]>tnd+"01" AND latgz[ki,8]=tnd
	   	        m.ze=m.ze+(m.jbgz+m.jt)*(VAL(latgz[ki,9])-m.start)&&绩效折算成100%
		        m.y1=m.y1+VAL(latgz[ki,9])-m.start
	            m.start=VAL(latgz[ki,9])
		    ELSE
		        IF latgz[ki,8]>tnd
		            EXIT
		        ENDIF
        	    m.start=1
		    ENDIF
		    m.jt=latgz[ki,7]
		    m.jbgz=latgz[ki,6]
		    m.zwbm=latgz[ki,3]
		ENDFOR
	    m.ze=m.ze+(m.jbgz+m.jt)*(13-m.start)
	    m.y1=m.y1+(13-m.start)
		IF LEFT(m.zwbm,2)<"07" AND AT("F",m.zwbm)<=0 AND INLIST(dkhjg(dwbm+grbm,"2014"),'优秀','合格','称职')
		    m.ze=m.ze+m.jbgz
		ENDIF

    ENDIF
	
	IF m.ze>0
	    m.retv=m.retv+","+STR(INT(m.ze/y1+0.5))
	ELSE
	    m.retv=m.retv+",0000"
	ENDIF

    &&2015
    m.ys=0
    m.ze=0    
    tnd="2015"
	m.jbgz=latgz[1,6]
	m.jt=latgz[1,7]
	m.y1=0

    IF latgz[1,8]>"2015"&&2016后进入
        m.ze=0
    ELSE
	    m.start=VAL(latgz[1,9])
	        	
		FOR ki=1 TO ALEN(latgz,1)
		    IF latgz[ki,8]+latgz[ki,9]>tnd+"01" AND latgz[ki,8]=tnd
	   	        m.ze=m.ze+(m.jbgz+m.jt)*(VAL(latgz[ki,9])-m.start)&&绩效折算成100%
		        m.y1=m.y1+VAL(latgz[ki,9])-m.start
	            m.start=VAL(latgz[ki,9])
		    ELSE
		        IF latgz[ki,8]>tnd
		            EXIT
		        ENDIF
        	    m.start=1
		    ENDIF
		    m.jt=latgz[ki,7]
		    m.jbgz=latgz[ki,6]
		    m.zwbm=latgz[ki,3]
		ENDFOR
	    m.ze=m.ze+(m.jbgz+m.jt)*(13-m.start)
	    m.y1=m.y1+(13-m.start)
		IF LEFT(m.zwbm,2)<"07" AND AT("F",m.zwbm)<=0 AND INLIST(dkhjg(dwbm+grbm,"2015"),'优秀','合格','称职')
		    m.ze=m.ze+m.jbgz
		ENDIF

    ENDIF

	IF m.ze>0
	    m.retv=m.retv+","+STR(INT(m.ze/y1+0.5))
	ELSE
	    m.retv=m.retv+",0000"
	ENDIF

    &&2016
    m.ys=0
    m.ze=0    
    tnd="2016"
	m.jbgz=latgz[1,6]
	m.jt=latgz[1,7]
	m.y1=0

    IF latgz[1,8]>"2016"&&2017后进入
        m.ze=0
    ELSE
	    m.start=VAL(latgz[1,9])
	        	
		FOR ki=1 TO ALEN(latgz,1)
		    IF latgz[ki,8]+latgz[ki,9]>tnd+"01" AND latgz[ki,8]=tnd
	   	        m.ze=m.ze+(m.jbgz+m.jt)*(VAL(latgz[ki,9])-m.start)&&绩效折算成100%
		        m.y1=m.y1+VAL(latgz[ki,9])-m.start
	            m.start=VAL(latgz[ki,9])
		    ELSE
		        IF latgz[ki,8]>tnd
		            EXIT
		        ENDIF
        	    m.start=1
		    ENDIF
		    m.jt=latgz[ki,7]
		    m.jbgz=latgz[ki,6]
		    m.zwbm=latgz[ki,3]
		ENDFOR
	    m.ze=m.ze+(m.jbgz+m.jt)*(13-m.start)
	    m.y1=m.y1+(13-m.start)
		IF LEFT(m.zwbm,2)<"07" AND AT("F",m.zwbm)<=0 AND INLIST(dkhjg(dwbm+grbm,"2016"),'优秀','合格','称职')
		    m.ze=m.ze+m.jbgz
		ENDIF

    ENDIF

	IF m.ze>0
	    m.retv=m.retv+","+STR(INT(m.ze/y1+0.5))
	ELSE
	    m.retv=m.retv+",0000"
	ENDIF
	
    RETURN m.retv
ELSE
    RETURN "0000,0000,0000"
ENDIF
