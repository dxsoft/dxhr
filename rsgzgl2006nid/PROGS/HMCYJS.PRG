***** 计算花名册右边
SELECT field_name,lrfs,gld,jxryff,qsff,gdz,jbt FROM fldgz WHERE sfsy="√" AND field_type="N" AND (category=IIF(m.pdwbz='行政',"01","10") OR category="00") ORDER BY sequence INTO ARRAY laFldName

m.yjxjt=ryjbxx.jxjt

FOR i=1 TO ALEN(laFLdName,1)
    IF AT("手工",laFLdName[i,2])>0&& AND laFldName[i,7]&&保存手工录入值
        m .&laFldName[i,1]=ryjbxx. &laFldname[i,1]
    ENDIF
    
    IF UPPER(laFLdName[i,1])="PGBC"
        m.pgbclrfs=laFLdName[i,2]
    ENDIF
ENDFOR

m.jjjy=ryjbxx.jjjy2

SELECT field_name FROM fldgz WHERE field_type="N" INTO ARRAY laFldName1

FOR i=1 TO _tally&&工资清零
    REPLACE ryjbxx. &laFldName1[i,1] WITH 0 IN ryjbxx
ENDFOR
RELEASE laFldname1

IF EMPTY(ryjbxx.zwgw2)
    RETURN
ENDIF

v_zwbm=ryjbxx.zwbm2
 
IF EMPTY(v_zwbm)
    RETURN
ENDIF

v_tbnd=ryjbxx.tbnd

&&见习人员工资
IF RIGHT(v_zwbm,2)='99'

    IF cyxx.jxgz=1
	    IF pnYEAR+pnMONTH/100-VAL(ryjbxx.cjgzny)>=1
	        nd="2"
	    ELSE
	        nd="1"
	    ENDIF

	    v_xl=ALLTRIM(SUBSTR(xl(ryjbxx.dwbm+ryjbxx.grbm,STRTRAN(ryjbxx.cjgzny,".")),7))
	    REPLACE ryjbxx.jxgz WITH jxgz(v_tbnd,ryjbxx.dwsx,v_xl,nd)

	    &&&&津补贴
	    v_zwbm=IIF(ryjbxx.zwbm2<"1000",ryjbxx.zwbm2,"10"+RIGHT(ryjbxx.zwbm2,2))
	    FOR i=1 TO ALEN(laFLdName,1)
	        IF AT("自动",laFLdName[i,2])>0 AND laFldName[i,7] AND laFldName[i,4]=1
	            isfound=.F.
	            isFound=gzbzdw("jbtbz",v_zwbm,ryjbxx.jbtbz)
	            IF isFound    
	                REPLACE &laFLdName[i,1] WITH jbtbz. &laFLdName[i,1] IN ryjbxx
		        ENDIF
		        
	        ELSE
	            IF AT("手工",laFLdName[i,2])>0
	                REPLACE &laFLdName[i,1] WITH m. &laFLdName[i,1] IN ryjbxx
	            ENDIF
	        ENDIF
	    ENDFOR
	ENDIF
ELSE &&转正人员工资
	IF EMPTY(ryjbxx.zwgzdc2)
	    RETURN
	ENDIF
	
	REPLACE zwgzse2 with zwgzse(ryjbxx.zwbm2,ryjbxx.zwgzdc2,ryjbxx.tbnd) IN ryjbxx
    IF LEFT(ryjbxx.dwsx,2)="01"
		REPLACE jbgzse2 WITH jbgzse(ryjbxx.tbnd,ryjbxx.jbgzjb2,ryjbxx.djc2) IN ryjbxx 
		REPLACE jcgz2 WITH jcgz(ryjbxx.tbnd) IN ryjbxx
		REPLACE glgz2 WITH glgz(ryjbxx.tbnd,ryjbxx.gznx) IN ryjbxx
	ENDIF
	
	IF !INLIST(ryjbxx.dwsx,"01","02","03","04","05","06") AND !EMPTY(ryjbxx.jtbl)
    	m.njtbl=VAL(LEFT(ryjbxx.jtbl,AT("/",ryjbxx.jtbl)-1))/VAL(SUBSTR(ryjbxx.jtbl,AT("/",ryjbxx.jtbl)+1))
	ELSE
	    m.nJtbl=0
	ENDIF
	
	IF !EMPTY(ryjbxx.fddc)
	    REPLACE fdgz2 WITH fdgz(ryjbxx.tbnd,ryjbxx.zwbm2,ryjbxx.zwgzdc2,ryjbxx.fddc,m.njtbl) IN ryjbxx
    ENDIF
    
	**计算奖金结余
    
    IF m.jjjy>0
        REPLACE jjjy2 WITH m.jjjy IN ryjbxx
    ELSE
        REPLACE jjjy2 WITH jjjy(ryjbxx.zwbm2,ryjbxx.gznx,ryjbxx.cjgzny,ryjbxx.zdgznx,ryjbxx.dwbm,ryjbxx.grbm,ryjbxx.dwsx) IN ryjbxx
    ENDIF
    
    IF LEFT(ryjbxx.zwbm2,2)="05"
        REPLACE jsdjgz2 WITH jsdjgz(ryjbxx.tbnd,ryjbxx.zwbm2) IN ryjbxx
    ENDIF
    
    IF LEFT(ryjbxx.zwbm2,2)="05" OR LEFT(ryjbxx.zwbm2,2)="06"
        REPLACE grjj2 WITH zround((ryjbxx.zwgzse2+ryjbxx.jsdjgz2)*3/7) IN ryjbxx
	ENDIF
	
	REPLACE jt2 WITH zround(ryjbxx.zwgzse2*m.nJtbl) IN ryjbxx 
	IF m.pdwbz<>"行政"
    	REPLACE jsfszwtg2 WITH zround(ryjbxx.zwgzse2*ryjbxx.tgbl/100) IN ryjbxx
    ELSE
    	REPLACE tgbl WITH 0 IN ryjbxx
    ENDIF

	&&津补贴计算
    v_zwbm=IIF(ryjbxx.zwbm2<"1000",ryjbxx.zwbm2,"10"+RIGHT(ryjbxx.zwbm2,2))
    
    FOR i=1 TO ALEN(laFLdName,1)
        IF AT("自动",laFLdName[i,2])>0 AND laFldName[i,7]
            isfound=.F.
            IF AT("现任",lafldName[i,5])>0&&现任职务
                isFound=gzbzdw("jbtbz",ryjbxx.zjbm,ryjbxx.jbtbz)
            ELSE
                isFound=gzbzdw("jbtbz",v_zwbm,ryjbxx.jbtbz)
            ENDIF
            IF isFound    
			    IF !EMPTY(laFLdName[i,3])&&分工龄段:10,10,05表示从10年开始每10年增加05元
			        v_gl=ryjbxx.gznx
			        v_qsgl=val(left(laFLdName[i,3],2))
			        v_gld=val(subs(laFLdName[i,3],4,2))
			        v_gzse=val(subs(laFLdName[i,3],7,2))
			        IF v_gl>v_qsgl
			            zjz=v_gzse*IIF((v_gl-v_qsgl)/v_gld=INT((v_gl-v_qsgl)/v_gld),(v_gl-v_qsgl)/v_gld,INT((v_gl-v_qsgl)/v_gld)+1)
			            REPLACE &laFLdName[i,1] WITH jbtbz. &laFLdName[i,1]+zjz IN ryjbxx
			        ELSE
			            REPLACE &laFLdName[i,1] WITH jbtbz. &laFLdName[i,1] IN ryjbxx
			        ENDIF
			    ELSE
	                REPLACE &laFLdName[i,1] WITH jbtbz. &laFLdName[i,1] IN ryjbxx
	            ENDIF
	        ENDIF
	        
        ELSE
            IF AT("手工",laFLdName[i,2])>0
                REPLACE &laFLdName[i,1] WITH m. &laFLdName[i,1] IN ryjbxx
            ENDIF
            
        ENDIF
    ENDFOR

	&&警衔津补计算
    &&jxjttbnd=tbnd(STR(m.pnYear,4)+RIGHT(ALLTRIM(STR(100+m.pnMonth)),2),"jxjtbz")
    REPLACE jxjt WITH jxjt(ryjbxx.jxjtbz,ryjbxx.jx) IN ryjbxx

ENDIF

************
IF m.tDfbt=0
     REPLACE ryjbxx.dfbt2 WITH 0 IN ryjbxx
ENDIF
    
m.hjgz=0
FOR i=1 TO ALEN(laFldName,1)
    m.hjgz=m.hjgz+ryjbxx. &laFldName[i,1]
ENDFOR

REPLACE hj2 with m.hjgz in ryjbxx

SELECT ryjbxx

&&聘干补差
IF zc_pgbc=1 AND AT("自动",m.pgbclrfs)>0
	IF ryjbxx.jslb="聘干定资"
		IF ryjbxx.dwsx="01"
			m.bc=(ryjbxx.zwgzse1+ryjbxx.jsdjgz1+ryjbxx.grjj1) - (ryjbxx.zwgzse2+ryjbxx.jbgzse2+ryjbxx.jcgz2+ryjbxx.glgz2)
		ELSE
			m.bc=(ryjbxx.zwgzse1+ryjbxx.jsdjgz1) - ryjbxx.zwgzse2
		ENDIF
		REPLACE hj1 WITH hj1-pgbc1 IN ryjbxx
		REPLACE pgbc1 WITH 0 IN ryjbxx
		REPLACE ryjbxx.pgbc WITH IIF(m.bc>0,m.bc,0) IN ryjbxx
	    REPLACE ryjbxx.hj2 WITH ryjbxx.hj2+ryjbxx.pgbc IN ryjbxx
	ELSE&&冲销
	    IF ryjbxx.pgbc=0 AND ryjbxx.pgbc1>0
	         IF ryjbxx.hj2-ryjbxx.hj1+ryjbxx.pgbc1>ryjbxx.pgbc1
	             REPLACE pgbc WITH 0 IN ryjbxx
	         ELSE
	             REPLACE pgbc WITH ryjbxx.hj1-ryjbxx.hj2 IN ryjbxx
	             REPLACE hj2 WITH hj1 IN ryjbxx
	         ENDIF
	    ENDIF
	ENDIF
ENDIF

REPLACE sfgz WITH hj2-(grsds+zfgjj+ylbxf+ylf+qtdk)+bfyqgz-kjyqgz IN ryjbxx
