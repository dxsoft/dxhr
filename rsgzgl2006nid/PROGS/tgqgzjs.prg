***** 计算花名册右边
v_zwbm=tgqgz2006.zwbm2

SELECT field_name,lrfs,gld,jxryff,qsff,gdz,jbt FROM fldgz WHERE sfsy="√" AND field_type="N" AND UPPER(field_name)<>"TGBL" AND UPPER(field_name)<>"NJBT" AND (category=IIF(INLIST(v_zwbm,"01","02","03","05","06"),"01","10") OR category="00") ORDER BY sequence INTO ARRAY laFldName

m.yjxjt=tgqgz2006.jxjt

FOR i=1 TO ALEN(laFLdName,1)
    IF AT("手工",laFLdName[i,2])>0&& AND laFldName[i,7]&&保存手工录入值
        m .&laFldName[i,1]=tgqgz2006. &laFldname[i,1]
    ENDIF
    
    IF UPPER(laFLdName[i,1])="PGBC"
        m.pgbclrfs=laFLdName[i,2]
    ENDIF
ENDFOR

REPLACE gznx WITH 2006-VAL(LEFT(ryjbxx.cjgzny,4))+1-ryjbxx.zdgznx IN tgqgz2006
m.jjjy=tgqgz2006.jjjy2
m.jxgz=tgqgz2006.jxgz
m.tgbl=tgqgz2006.tgbl

SELECT field_name,000.0 FROM fldgz WHERE field_type="N" AND sfsy="√" AND UPPER(field_name)<>"TGBL" INTO ARRAY laFldName1

FOR i=1 TO _tally&&工资清零
    laFldname1[i,2]=tgqgz2006. &laFldName1[i,1]
    REPLACE tgqgz2006. &laFldName1[i,1] WITH 0 IN tgqgz2006
ENDFOR

IF EMPTY(tgqgz2006.zwgw2)
    RETURN
ENDIF

IF EMPTY(v_zwbm)
    RETURN
ENDIF


v_tbnd="200307"

&&见习人员工资
IF RIGHT(v_zwbm,2)='FF'

    IF cyxx.jxgz=1
	    IF pnYEAR+pnMONTH/100-VAL(ryjbxx.cjgzny)>=1
	        nd="2"
	    ELSE
	        nd="1"
	    ENDIF

	    v_xl=ALLTRIM(SUBSTR(xl(tgqgz2006.dwbm+tgqgz2006.grbm,STRTRAN(ryjbxx.cjgzny,".")),7))
	    IF cyxx.jxgz=1
    	    REPLACE tgqgz2006.jxgz WITH jxgz(v_tbnd,LEFT(tgqgz2006.zwbm2,2),v_xl,nd)
    	ELSE
    	    REPLACE tgqgz2006.jxgz WITH m.jxgz
        ENDIF
	    &&&&津补贴
	    v_zwbm=IIF(tgqgz2006.zwbm2<"1000",tgqgz2006.zwbm2,"10"+RIGHT(tgqgz2006.zwbm2,2))
	    FOR i=1 TO ALEN(laFLdName,1)
	        IF AT("自动",laFLdName[i,2])>0 AND CAST(laFldName[i,7] as i)=1 AND CAST(laFldName[i,4] as i)=1
	            isfound=.F.
	            isFound=gzbzdw("jbtbz",v_zwbm,tgqgz2006.jbtbz)
	            IF isFound    
	                REPLACE tgqgz2006. &laFLdName[i,1] WITH jbtbz. &laFLdName[i,1] IN tgqgz2006
		        ENDIF
		        
	        ELSE
	            IF AT("手工",laFLdName[i,2])>0
	                REPLACE tgqgz2006. &laFLdName[i,1] WITH m. &laFLdName[i,1] IN tgqgz2006
	            ENDIF
	        ENDIF
	    ENDFOR
	ENDIF
ELSE &&转正人员工资
	IF EMPTY(tgqgz2006.zwgzdc2)
	    RETURN
	ENDIF
	
	REPLACE tgqgz2006.zwgzse2 with zwgzse(tgqgz2006.zwbm2,tgqgz2006.zwgzdc2,"200307") IN tgqgz2006
    IF LEFT(tgqgz2006.zwbm2,2)="01"
		REPLACE tgqgz2006.jbgzse2 WITH jbgzse("200307",tgqgz2006.jbgzjb2,tgqgz2006.djc2) IN tgqgz2006 
		REPLACE tgqgz2006.jcgz2 WITH jcgz("200307") IN tgqgz2006
		REPLACE tgqgz2006.glgz2 WITH glgz("200307",tgqgz2006.gznx) IN tgqgz2006
	ENDIF
	
	IF LEFT(v_zwbm,2)<>"01" AND !EMPTY(tgqgz2006.jtbl)
    	m.njtbl=VAL(LEFT(tgqgz2006.jtbl,AT("/",tgqgz2006.jtbl)-1))/VAL(SUBSTR(tgqgz2006.jtbl,AT("/",tgqgz2006.jtbl)+1))
	ELSE
	    m.nJtbl=0
	ENDIF
	
	IF !EMPTY(tgqgz2006.fddc)
	    REPLACE tgqgz2006.fdgz2 WITH fdgz("200307",tgqgz2006.zwbm2,tgqgz2006.zwgzdc2,tgqgz2006.fddc,m.njtbl) IN tgqgz2006
    ENDIF
    
	**计算奖金结余
    
*!*	    IF m.jjjy>0
        REPLACE tgqgz2006.jjjy2 WITH m.jjjy IN tgqgz2006

*!*	    ELSE
*!*	        REPLACE jjjy2 WITH jjjy(tgqgz2006.zwbm2,tgqgz2006.gznx,ryjbxx.cjgzny,tgqgz2006.zdgznx,tgqgz2006.dwbm,tgqgz2006.grbm,tgqgz2006.dwsx) IN tgqgz2006
*!*	    ENDIF
    REPLACE tgqgz2006.tgbl WITH m.tgbl IN tgqgz2006
    
    REPLACE tgqgz2006.jsdjgz2 WITH jsdjgz("200307",tgqgz2006.zwbm2) IN tgqgz2006
    
    IF LEFT(tgqgz2006.zwbm2,2)="05" OR LEFT(tgqgz2006.zwbm2,2)="06"
        REPLACE tgqgz2006.grjj2 WITH (tgqgz2006.zwgzse2+tgqgz2006.jsdjgz2)*3/7 IN tgqgz2006
	ENDIF

	REPLACE tgqgz2006.jt2 WITH zround(tgqgz2006.zwgzse2*m.nJtbl) IN tgqgz2006 
	
	IF m.pdwbz<>"行政"
    	REPLACE tgqgz2006.jsfszwtg2 WITH zround(tgqgz2006.zwgzse2*tgqgz2006.tgbl/100) IN tgqgz2006
    ELSE
    	REPLACE tgqgz2006.tgbl WITH 0 IN tgqgz2006
    ENDIF

	&&津补贴计算
    v_zwbm=IIF(tgqgz2006.zwbm2<"1000",tgqgz2006.zwbm2,"10"+RIGHT(tgqgz2006.zwbm2,2))
    
    FOR i=1 TO ALEN(laFLdName,1)
        IF AT("自动",laFLdName[i,2])>0 AND CAST(laFldName[i,7] as i)=1
            isfound=.F.
            IF AT("现任",lafldName[i,5])>0&&现任职务
                isFound=gzbzdw("jbtbz",ryjbxx.zjbm,tgqgz2006.jbtbz)
            ELSE
                isFound=gzbzdw("jbtbz",v_zwbm,tgqgz2006.jbtbz)
            ENDIF
            IF isFound    
			    IF !EMPTY(laFLdName[i,3])&&分工龄段:10,10,05表示从10年开始每10年增加05元
			        v_gl=tgqgz2006.gznx
			        v_qsgl=val(left(laFLdName[i,3],2))
			        v_gld=val(subs(laFLdName[i,3],4,2))
			        v_gzse=val(subs(laFLdName[i,3],7,2))
			        IF v_gl>v_qsgl
			            zjz=v_gzse*IIF((v_gl-v_qsgl)/v_gld=INT((v_gl-v_qsgl)/v_gld),(v_gl-v_qsgl)/v_gld,INT((v_gl-v_qsgl)/v_gld)+1)
			            REPLACE tgqgz2006. &laFLdName[i,1] WITH jbtbz. &laFLdName[i,1]+zjz IN tgqgz2006
			        ELSE
			            REPLACE tgqgz2006. &laFLdName[i,1] WITH jbtbz. &laFLdName[i,1] IN tgqgz2006
			        ENDIF
			    ELSE
	                REPLACE tgqgz2006. &laFLdName[i,1] WITH jbtbz. &laFLdName[i,1] IN tgqgz2006
	            ENDIF
	        ENDIF
	        
        ELSE
            IF AT("手工",laFLdName[i,2])>0
                REPLACE tgqgz2006. &laFLdName[i,1] WITH m. &laFLdName[i,1] IN tgqgz2006
            ENDIF
            
        ENDIF
    ENDFOR

	&&警衔津补计算
    REPLACE tgqgz2006. jxjt WITH jxjt(tgqgz2006.jxjtbz,tgqgz2006.jx) IN tgqgz2006

	&&教护龄津贴计算
    IF VAL(LEFT(ryjbxx.jhlqsny,4))>0
        v_jhl=2006-VAL(LEFT(ryjbxx.jhlqsny,4))-1 - ryjbxx.zdjhlnx
        DO case
        CASE v_jhl<5
            REPLACE tgqgz2006.jhljt WITH 0 IN tgqgz2006
        CASE v_jhl<10
            REPLACE tgqgz2006.jhljt WITH 3 IN tgqgz2006
        CASE v_jhl<15
            REPLACE tgqgz2006.jhljt WITH 5 IN tgqgz2006
        CASE v_jhl<20
            REPLACE tgqgz2006.jhljt WITH 7 IN tgqgz2006
        OTHERWISE
            REPLACE tgqgz2006.jhljt WITH 10 IN tgqgz2006
        ENDCASE
    ENDIF
ENDIF

************
REPLACE blfb2 WITH blfb(tgqgz2006.zwbm2) IN tgqgz2006

IF m.tDfbt=0
     REPLACE tgqgz2006.dfbt2 WITH 0 IN tgqgz2006
ELSE
	SELECT bz FROM bz06_jbt WHERE UPPER(item)=="DFBT2" AND zwbm=tgqgz2006.zwbm2 AND tbnd='200501' INTO ARRAY latdfbt
	IF _tally>0
        REPLACE tgqgz2006.dfbt2 WITH latdfbt[1] IN tgqgz2006
	ENDIF
ENDIF
    
m.hjgz=0
FOR i=1 TO ALEN(laFldName,1)
    m.hjgz=m.hjgz+tgqgz2006. &laFldName[i,1]
ENDFOR

REPLACE hj2 with m.hjgz in tgqgz2006

RELEASE laFldname1
RELEASE laFldname

SELECT tgqgz2006