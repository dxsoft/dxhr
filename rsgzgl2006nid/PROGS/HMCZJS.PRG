***** 计算花名册右边
SELECT field_name,lrfs,gld,jxryff,qsff,gdz,jbt FROM fldgz WHERE sfsy="√" AND field_type="N" AND (category=IIF(m.pdwbz='行政',"01","10") OR category="00") ORDER BY sequence INTO ARRAY laFldName

m.yjxjt=ryjbxx.jxjt

FOR i=1 TO ALEN(laFLdName,1)
    laFldName[i,1]=ALLTRIM(STRTRAN(laFldName[i,1],"2",""))+"1"
    IF AT("手工",laFLdName[i,2])>0&& AND laFldName[i,7]&&保存手工录入值
        m .&laFldName[i,1]=ryjbxx. &laFldname[i,1]
    ENDIF
    
    IF UPPER(laFLdName[i,1])="PGBC"
        m.pgbclrfs=laFLdName[i,2]
    ENDIF
ENDFOR

m.jjjy=ryjbxx.jjjy1

SELECT field_name FROM fldgz WHERE field_type="N" INTO ARRAY laFldName1

FOR i=1 TO _tally&&工资清零
    REPLACE ryjbxx. &laFldName1[i,1] WITH 0 IN ryjbxx
ENDFOR
RELEASE laFldname1

IF EMPTY(ryjbxx.zwgw1)
    RETURN
ENDIF

v_zwbm=ryjbxx.zwbm1
 
IF EMPTY(v_zwbm)
    RETURN
ENDIF

v_tbnd=ryjbxx.tbnd1

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
	    v_zwbm=IIF(ryjbxx.zwbm1<"1000",ryjbxx.zwbm1,"10"+RIGHT(ryjbxx.zwbm1,2))
	    FOR i=1 TO ALEN(laFLdName,1)
	        IF AT("自动",laFLdName[i,2])>0 AND laFldName[i,7] AND laFldName[i,4]=1
	            isfound=.F.
	            isFound=gzbzdw("jbtbz",v_zwbm,ryjbxx.jbtbz1)
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
	IF EMPTY(ryjbxx.zwgzdc1)
	    RETURN
	ENDIF
	
	REPLACE zwgzse1 with zwgzse(ryjbxx.zwbm1,ryjbxx.zwgzdc1,ryjbxx.tbnd1) IN ryjbxx
    IF LEFT(ryjbxx.dwsx,2)="01"
		REPLACE jbgzse1 WITH jbgzse(ryjbxx.tbnd1,ryjbxx.jbgzjb1,ryjbxx.djc1) IN ryjbxx 
		REPLACE jcgz1 WITH jcgz(ryjbxx.tbnd1) IN ryjbxx
		REPLACE glgz1 WITH glgz(ryjbxx.tbnd1,ryjbxx.gznx) IN ryjbxx
	ENDIF
	
	IF m.pdwbz<>"行政" AND !EMPTY(ryjbxx.jtbl)
    	m.njtbl=VAL(LEFT(ryjbxx.jtbl,AT("/",ryjbxx.jtbl)-1))/VAL(SUBSTR(ryjbxx.jtbl,AT("/",ryjbxx.jtbl)+1))
	ELSE
	    m.nJtbl=0
	ENDIF
	
	IF !EMPTY(ryjbxx.fddc)
	    REPLACE fdgz1 WITH fdgz(ryjbxx.tbnd1,ryjbxx.zwbm1,ryjbxx.zwgzdc1,ryjbxx.fddc,m.njtbl) IN ryjbxx
    ENDIF
    
	**计算奖金结余
    
    IF m.jjjy>0
        REPLACE jjjy1 WITH m.jjjy IN ryjbxx
    ELSE
        REPLACE jjjy1 WITH jjjy(ryjbxx.zwbm1,ryjbxx.gznx,ryjbxx.cjgzny,ryjbxx.zdgznx,ryjbxx.dwbm,ryjbxx.grbm,ryjbxx.dwsx) IN ryjbxx
    ENDIF
    
    IF LEFT(ryjbxx.zwbm1,2)="05"
        REPLACE jsdjgz1 WITH jsdjgz(ryjbxx.tbnd1,ryjbxx.zwbm1) IN ryjbxx
    ENDIF
    
    IF LEFT(ryjbxx.zwbm1,2)="05" OR LEFT(ryjbxx.zwbm1,2)="06"
        REPLACE grjj1 WITH zround((ryjbxx.zwgzse1+ryjbxx.jsdjgz1)*3/7) IN ryjbxx
	ENDIF
		
	REPLACE jt1 WITH zround(ryjbxx.zwgzse1*m.nJtbl) IN ryjbxx 
	IF m.pdwbz<>"行政"
    	REPLACE jsfszwtg1 WITH zround(ryjbxx.zwgzse1*ryjbxx.tgbl/100) IN ryjbxx
    ELSE
    	REPLACE tgbl WITH 0 IN ryjbxx
    ENDIF

	&&津补贴计算
    v_zwbm=IIF(ryjbxx.zwbm1<"1000",ryjbxx.zwbm1,"10"+RIGHT(ryjbxx.zwbm1,2))
    
    FOR i=1 TO ALEN(laFLdName,1)
        IF AT("自动",laFLdName[i,2])>0 AND laFldName[i,7]
            isfound=.F.
            IF AT("现任",lafldName[i,5])>0&&现任职务
                isFound=gzbzdw("jbtbz",ryjbxx.zjbm,ryjbxx.jbtbz1)
            ELSE
                isFound=gzbzdw("jbtbz",v_zwbm,ryjbxx.jbtbz1)
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
    REPLACE jxjt WITH jxjt(ryjbxx.jxjtbz1,ryjbxx.jx) IN ryjbxx

ENDIF

************
IF m.tDfbt=0
     REPLACE ryjbxx.dfbt1 WITH 0 IN ryjbxx
ENDIF
    
m.hjgz=0
FOR i=1 TO ALEN(laFldName,1)
    m.hjgz=m.hjgz+ryjbxx. &laFldName[i,1]
ENDFOR

REPLACE hj1 with m.hjgz in ryjbxx

SELECT ryjbxx
