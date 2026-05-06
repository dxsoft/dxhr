***** 计算花名册右边
SELECT field_name,lrfs,gld,jxryff,qsff,gdz,jbt FROM fldgz WHERE sfsy06="√" AND field_type="N" AND UPPER(field_name)<>"HJ2" ORDER BY sequence INTO ARRAY laFldName

m.yjxjt=ryjbxx.jxjt

FOR i=1 TO ALEN(laFLdName,1)
    IF AT("手工",laFLdName[i,2])>0&&保存手工录入值
        m .&laFldName[i,1]=ryjbxx. &laFldname[i,1]
    ENDIF
    
    IF UPPER(laFLdName[i,1])="PGBC"
        m.pgbclrfs=laFLdName[i,2]
    ENDIF
ENDFOR

m.jjjy=ryjbxx.jjjy2
m.ytgbl=ryjbxx.tgblbf
m.jxgz=ryjbxx.jxgz
m.jhljt=ryjbxx.jhljt
m.tgbl=ryjbxx.tgbl

m.gwjt=ryjbxx.gwjt2
m.qtbt=ryjbxx.qtbt

SELECT field_name FROM fldgz WHERE field_type="N" AND UPPER(field_name)<>"TGBL" INTO ARRAY laFldName1
v_reccnt=_tally
FOR i=1 TO v_reccnt&&工资清零
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
IF AT("F",v_zwbm)>0

    v_xl=ALLTRIM(SUBSTR(xl(ryjbxx.dwbm+ryjbxx.grbm,STRTRAN(ryjbxx.cjgzny,".")),9))
    IF cyxx.jxgz=1
        REPLACE ryjbxx.jxgz WITH jxgz06(v_tbnd,ryjbxx.zwbm2,v_xl,ryjbxx.cjgzny) IN ryjbxx
    ELSE
        REPLACE ryjbxx.jxgz WITH m.jxgz IN ryjbxx
    ENDIF

   	IF LEFT(v_zwbm,2)>="07"
   	    IF BITTEST(cyxx.zzrs,0)&&见习工资提高
   	       	REPLACE jsfszwtg2 WITH zround(ryjbxx.jxgz*ryjbxx.tgbl/100) IN ryjbxx
   	    ELSE
   	       	REPLACE jsfszwtg2 WITH 0 IN ryjbxx
   	    ENDIF
   	ENDIF
   	IF !EMPTY(v_zwbm) AND LEFT(v_zwbm,2)<"07"&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
       	REPLACE tgbl WITH 0 IN ryjbxx
    ENDIF
   	
	&&津补贴计算
    FOR i=1 TO ALEN(laFLdName,1)
        IF AT("自动",laFLdName[i,2])>0 AND laFldName[i,7] AND laFldName[i,4]=1&&见习人员发放
            oldalias=ALIAS()
            IF lafldName[i,5]="统一值"
                REPLACE &laFLdName[i,1] WITH lafldName[i,6] IN ryjbxx
            ELSE
                v_zwbm=IIF(ryjbxx.zwbm2<"1000",ryjbxx.zwbm2,"10"+RIGHT(ryjbxx.zwbm2,2))
	            SELECT bz06_jbt
	            IF ALLTRIM(UPPER(laFLdName[i,1]))="JHLJT"
	    		    LOCATE FOR BETWEEN(VAL(LEFT(m.rq,4))-VAL(LEFT(ryjbxx.jhlqsny,4)),worklower,workupper) AND UPPER(item)=ALLTRIM(UPPER(laFLdName[i,1])) AND (zwbm=v_zwbm OR EMPTY(zwbm)) AND tbnd=ryjbxx.jbtbz
	    		ELSE
		            IF ryjbxx.jbtbz>="200901"&& and INLIST(ALLTRIM(UPPER(laFLdName[i,1])),"DFBT2","BLFB2")
    	    		    LOCATE FOR BETWEEN(ryjbxx.gznx,worklower,workupper) AND UPPER(item)=ALLTRIM(UPPER(laFLdName[i,1])) AND (zwbm=LEFT(v_zwbm,3)+xlcc(v_xl) OR zwbm=v_zwbm OR EMPTY(zwbm)) AND tbnd=ryjbxx.jbtbz AND jxlb=jxlb(ryjbxx.dwbm)
    	    		ELSE
    	    		    LOCATE FOR BETWEEN(ryjbxx.gznx,worklower,workupper) AND UPPER(item)=ALLTRIM(UPPER(laFLdName[i,1])) AND (LEFT(zwbm,3)=LEFT(v_zwbm,3) OR EMPTY(zwbm)) AND tbnd=ryjbxx.jbtbz
    	    		ENDIF
	    		ENDIF
	            IF !EMPTY(oldalias)
	                SELECT (oldalias)
	            ENDIF
			    IF FOUND("bz06_jbt")
	                REPLACE &laFLdName[i,1] WITH bz06_jbt.bz IN ryjbxx
		        ENDIF
		    ENDIF
        ELSE
            IF AT("手工",laFLdName[i,2])>0
                REPLACE &laFLdName[i,1] WITH m. &laFLdName[i,1] IN ryjbxx
            ENDIF
        ENDIF
    ENDFOR

	&&警衔津贴计算
*!*	    REPLACE jxjt WITH jxjt(ryjbxx.jxjtbz,ryjbxx.jx)+jcjt(ryjbxx.jcjtbz,ryjbxx.jx)+spjt(ryjbxx.spjtbz,ryjbxx.jx) IN ryjbxx

    njbtbz=njbtbz(ryjbxx.dwbm)
    IF njbtbz>0
        REPLACE ryjbxx.njbt WITH njbt(ryjbxx.jbtbz,njbtbz) IN ryjbxx
    ELSE
        REPLACE ryjbxx.njbt WITH 0 IN ryjbxx
    ENDIF

ELSE &&转正人员工资
	IF EMPTY(ryjbxx.zwgzdc2)
	    RETURN
	ENDIF
	
	REPLACE zwgzse2 with zwgz06_gr(ryjbxx.zwbm2,ryjbxx.zwgzdc2,ryjbxx.djc2,ryjbxx.tbnd)+zwgz06(ryjbxx.zwbm2,ryjbxx.tbnd) IN ryjbxx
	REPLACE jbgzse2 WITH IIF(INLIST(LEFT(ryjbxx.zwbm2,2),"01","02","03"),jbgz06(ryjbxx.jbgzjb2,ALLTRIM(STR(VAL(ryjbxx.zwgzdc2)+VAL(ryjbxx.djc2))),ryjbxx.tbnd),0)+xjgz06(ryjbxx.zwgzdc2,ryjbxx.tbnd,ryjbxx.zwbm2) IN ryjbxx
	REPLACE jsdjgz2 WITH jsdjgz06(ryjbxx.zwbm2,ryjbxx.tbnd) IN ryjbxx 
	
	**计算奖金结余
    
    REPLACE jjjy2 WITH m.jjjy IN ryjbxx
    REPLACE tgbl WITH m.tgbl IN ryjbxx

	IF !EMPTY(ryjbxx.fddc)
	    REPLACE fdgz2 WITH fdgz06(ryjbxx.tbnd,ryjbxx.zwbm2,ryjbxx.zwgzdc2,ryjbxx.fddc) IN ryjbxx
    ENDIF
    
    IF !EMPTY(v_zwbm) AND LEFT(v_zwbm,2)<"07"&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
       	REPLACE tgbl WITH 0 IN ryjbxx
    ENDIF

   	IF LEFT(ryjbxx.zwbm2,2)>="07"
     	REPLACE jsfszwtg2 WITH zround((ryjbxx.zwgzse2+ryjbxx.jbgzse2)*ryjbxx.tgbl/100) IN ryjbxx
    ENDIF
    
	&&津补贴计算
    FOR i=1 TO ALEN(laFLdName,1)
        IF AT("自动",laFLdName[i,2])>0 AND laFldName[i,7]&&津补贴项
            oldalias=ALIAS()
            IF lafldName[i,5]="统一值"
                REPLACE &laFLdName[i,1] WITH lafldName[i,6] IN ryjbxx
            ELSE
	            IF AT("现任",lafldName[i,5])>0 AND ryjbxx.zwbm2<"1000"&&行政人员，现任职务
	                v_zwbm=ryjbxx.zjbm
	            ELSE
	                v_zwbm=IIF(ryjbxx.zwbm2<"1000",ryjbxx.zwbm2,"10"+RIGHT(ryjbxx.zwbm2,2))
	            ENDIF
	            SELECT bz06_jbt
	            IF ryjbxx.jbtbz>="200901"&& and INLIST(ALLTRIM(UPPER(laFLdName[i,1])),"DFBT2","BLFB2")
        		    LOCATE FOR BETWEEN(ryjbxx.gznx,worklower,workupper) AND UPPER(item)=ALLTRIM(UPPER(laFLdName[i,1])) AND (zwbm=v_zwbm OR EMPTY(zwbm)) AND tbnd=ryjbxx.jbtbz AND jxlb=jxlb(ryjbxx.dwbm)
        		ELSE
        		    LOCATE FOR BETWEEN(ryjbxx.gznx,worklower,workupper) AND UPPER(item)=ALLTRIM(UPPER(laFLdName[i,1])) AND (zwbm=v_zwbm OR EMPTY(zwbm)) AND tbnd=ryjbxx.jbtbz
        		ENDIF
	            IF !EMPTY(oldalias)
	                SELECT (oldalias)
	            ENDIF
			    IF FOUND("bz06_jbt")
	                REPLACE &laFLdName[i,1] WITH bz06_jbt.bz IN ryjbxx
		        ENDIF
		    ENDIF
        ELSE
            IF AT("手工",laFLdName[i,2])>0
                REPLACE &laFLdName[i,1] WITH m. &laFLdName[i,1] IN ryjbxx
            ENDIF
        ENDIF
    ENDFOR

	&&警衔津贴计算
    IF INLIST(LEFT(v_zwbm,2),'01','02','03')
        REPLACE jxjt WITH jxjt(ryjbxx.jxjtbz,ryjbxx.jx)+jcjt(ryjbxx.jcjtbz,ryjbxx.jx)+spjt(ryjbxx.spjtbz,ryjbxx.jx) IN ryjbxx
    ELSE
        REPLACE jxjt WITH 0 IN ryjbxx
    ENDIF
	IF AT("警",ryjbxx.jx)<=0
	    REPLACE ryjbxx.jxjtbz WITH "" IN ryjbxx
	ENDIF
	IF AT("法",ryjbxx.jx)<=0
	    REPLACE ryjbxx.spjtbz WITH "" IN ryjbxx
	ENDIF
	IF AT("检",ryjbxx.jx)<=0
	    REPLACE ryjbxx.jcjtbz WITH "" IN ryjbxx
	ENDIF

	&&教护龄津贴计算
    IF LEFT(v_zwbm,2)>="07"
&&   	    REPLACE jhljt WITH m.jhljt IN ryjbxx
        REPLACE ryjbxx.jhljt WITH jhljt(ryjbxx.jhlqsny,ryjbxx.zdjhlnx,ryjbxx.jsnf,v_zwbm) IN ryjbxx&&重算
    ELSE
        REPLACE ryjbxx.jhljt WITH 0 IN ryjbxx
    ENDIF

    njbtbz=njbtbz(ryjbxx.dwbm)
    IF njbtbz>0
        REPLACE ryjbxx.njbt WITH njbt(ryjbxx.jbtbz,njbtbz) IN ryjbxx
    ELSE
        REPLACE ryjbxx.njbt WITH 0 IN ryjbxx
    ENDIF

ENDIF

REPLACE gwjt2 WITH m.gwjt IN ryjbxx
REPLACE qtbt WITH m.qtbt IN ryjbxx

&&保留福补计算
REPLACE blfb2 WITH blfb(ryjbxx.zwbm2) IN ryjbxx

************
REPLACE ryjbxx.tgblbf WITH m.ytgbl IN ryjbxx
IF Dfbt(ryjbxx.dwbm)=0
     REPLACE ryjbxx.dfbt2 WITH 0 IN ryjbxx
ENDIF
    
m.hjgz=0
FOR i=1 TO ALEN(laFldName,1)
    m.hjgz=m.hjgz+ryjbxx. &laFldName[i,1]
ENDFOR

REPLACE hj2 with m.hjgz in ryjbxx


***** 计算花名册左边
m.yjxjt=ryjbxx.jxjt

FOR i=1 TO ALEN(laFLdName,1)
    IF AT("手工",laFLdName[i,2])>0&&保存手工录入值
        fldn=ALLTRIM(STRTRAN(laFldname[i,1],"2"))+"1"
        m .&laFldName[i,1]=ryjbxx. &fldn
    ENDIF
    
    IF UPPER(laFLdName[i,1])="PGBC"
        m.pgbclrfs=laFLdName[i,2]
    ENDIF
ENDFOR

m.jjjy1=ryjbxx.jjjy1
m.ytgbl1=ryjbxx.tgblbf1
m.jxgz1=ryjbxx.jxgz1
m.jhljt1=ryjbxx.jhljt1
m.tgbl1=ryjbxx.tgbl1
m.gwjt1=ryjbxx.gwjt1
m.qtbt1=ryjbxx.qtbt1

SELECT field_name FROM fldgz WHERE field_type="N" AND UPPER(field_name)<>"TGBL" INTO ARRAY laFldName1
v_reccnt=_tally
FOR i=1 TO v_reccnt&&工资清零
    fldn=ALLTRIM(STRTRAN(laFldName1[i,1],"2"))+"1"
    REPLACE ryjbxx. &fldn WITH 0 IN ryjbxx
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
IF AT("F",v_zwbm)>0

    v_xl=ALLTRIM(SUBSTR(xl(ryjbxx.dwbm+ryjbxx.grbm,STRTRAN(ryjbxx.cjgzny,".")),9))
    IF cyxx.jxgz=1
        REPLACE ryjbxx.jxgz1 WITH jxgz06(v_tbnd,ryjbxx.zwbm1,v_xl,ryjbxx.cjgzny) IN ryjbxx
    ELSE
        REPLACE ryjbxx.jxgz1 WITH m.jxgz IN ryjbxx
    ENDIF

   	IF LEFT(v_zwbm,2)>="07"
   	    IF BITTEST(cyxx.zzrs,0)&&见习工资提高
   	       	REPLACE jsfszwtg1 WITH zround(ryjbxx.jxgz1*ryjbxx.tgbl1/100) IN ryjbxx
   	    ELSE
   	       	REPLACE jsfszwtg1 WITH 0 IN ryjbxx
   	    ENDIF
   	ENDIF
   	IF !EMPTY(v_zwbm) AND LEFT(v_zwbm,2)<"07"&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
       	REPLACE tgbl1 WITH 0 IN ryjbxx
    ENDIF
   	
	&&津补贴计算
    FOR i=1 TO ALEN(laFLdName,1)
        IF AT("自动",laFLdName[i,2])>0 AND laFldName[i,7] AND laFldName[i,4]=1&&见习人员发放
            oldalias=ALIAS()
            IF lafldName[i,5]="统一值"
                fldn=ALLTRIM(STRTRAN(laFLdName[i,1],"2"))+"1"
                REPLACE &fldn WITH lafldName[i,6] IN ryjbxx
            ELSE
                v_zwbm=IIF(ryjbxx.zwbm1<"1000",ryjbxx.zwbm1,"10"+RIGHT(ryjbxx.zwbm1,2))
	            SELECT bz06_jbt
	            IF ALLTRIM(UPPER(laFLdName[i,1]))="JHLJT"
	    		    LOCATE FOR BETWEEN(VAL(LEFT(m.rq,4))-VAL(LEFT(ryjbxx.jhlqsny,4)),worklower,workupper) AND UPPER(item)=ALLTRIM(UPPER(laFLdName[i,1])) AND (zwbm=v_zwbm OR EMPTY(zwbm)) AND tbnd=ryjbxx.jbtbz1
	    		ELSE
		            IF ryjbxx.jbtbz>="200901"&& and INLIST(ALLTRIM(UPPER(laFLdName[i,1])),"DFBT2","BLFB2")
    	    		    LOCATE FOR BETWEEN(ryjbxx.gznx,worklower,workupper) AND UPPER(item)=ALLTRIM(UPPER(laFLdName[i,1])) AND (zwbm=LEFT(v_zwbm,3)+xlcc(v_xl) OR zwbm=v_zwbm OR EMPTY(zwbm)) AND tbnd=ryjbxx.jbtbz1 AND jxlb=jxlb(ryjbxx.dwbm)
    	    		ELSE
    	    		    LOCATE FOR BETWEEN(ryjbxx.gznx,worklower,workupper) AND UPPER(item)=ALLTRIM(UPPER(laFLdName[i,1])) AND (LEFT(zwbm,3)=LEFT(v_zwbm,3) OR EMPTY(zwbm)) AND tbnd=ryjbxx.jbtbz1
    	    		ENDIF
	    		ENDIF
	            IF !EMPTY(oldalias)
	                SELECT (oldalias)
	            ENDIF
			    IF FOUND("bz06_jbt")
			        fldn=ALLTRIM(STRTRAN(laFLdName[i,1],"2"))+"1"
	                REPLACE &fldn WITH bz06_jbt.bz IN ryjbxx
		        ENDIF
		    ENDIF
        ELSE
            IF AT("手工",laFLdName[i,2])>0
                fldn=ALLTRIM(STRTRAN(laFLdName[i,1],"2"))+"1"
                REPLACE &fldn WITH m. &fldn IN ryjbxx
            ENDIF
        ENDIF
    ENDFOR

	&&警衔津贴计算
*!*	    REPLACE jxjt WITH jxjt(ryjbxx.jxjtbz,ryjbxx.jx)+jcjt(ryjbxx.jcjtbz,ryjbxx.jx)+spjt(ryjbxx.spjtbz,ryjbxx.jx) IN ryjbxx

    njbtbz=njbtbz(ryjbxx.dwbm)
    IF njbtbz>0
        REPLACE ryjbxx.njbt1 WITH njbt(ryjbxx.jbtbz1,njbtbz) IN ryjbxx
    ELSE
        REPLACE ryjbxx.njbt1 WITH 0 IN ryjbxx
    ENDIF

ELSE &&转正人员工资
	IF EMPTY(ryjbxx.zwgzdc1)
	    RETURN
	ENDIF
	
	REPLACE zwgzse1 with zwgz06_gr(ryjbxx.zwbm1,ryjbxx.zwgzdc1,ryjbxx.djc1,ryjbxx.tbnd1)+zwgz06(ryjbxx.zwbm1,ryjbxx.tbnd1) IN ryjbxx
	REPLACE jbgzse1 WITH IIF(INLIST(LEFT(ryjbxx.zwbm1,2),"01","02","03"),jbgz06(ryjbxx.jbgzjb1,ALLTRIM(STR(VAL(ryjbxx.zwgzdc1)+VAL(ryjbxx.djc1))),ryjbxx.tbnd1),0)+xjgz06(ryjbxx.zwgzdc1,ryjbxx.tbnd1,ryjbxx.zwbm1) IN ryjbxx
	REPLACE jsdjgz1 WITH jsdjgz06(ryjbxx.zwbm1,ryjbxx.tbnd1) IN ryjbxx 
	
	**计算奖金结余
    
    REPLACE jjjy1 WITH m.jjjy IN ryjbxx
    REPLACE tgbl1 WITH m.tgbl IN ryjbxx

	IF !EMPTY(ryjbxx.fddc)
	    REPLACE fdgz1 WITH fdgz06(ryjbxx.tbnd1,ryjbxx.zwbm1,ryjbxx.zwgzdc1,ryjbxx.fddc) IN ryjbxx
    ENDIF
    
    IF !EMPTY(v_zwbm) AND LEFT(v_zwbm,2)<"07"&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
       	REPLACE tgbl1 WITH 0 IN ryjbxx
    ENDIF

   	IF LEFT(ryjbxx.zwbm1,2)>="07"
     	REPLACE jsfszwtg1 WITH zround((ryjbxx.zwgzse1+ryjbxx.jbgzse1)*ryjbxx.tgbl1/100) IN ryjbxx
    ENDIF
    
	&&津补贴计算
    FOR i=1 TO ALEN(laFLdName,1)
        IF AT("自动",laFLdName[i,2])>0 AND laFldName[i,7]&&津补贴项
            oldalias=ALIAS()
            IF lafldName[i,5]="统一值"
                fldn=ALLTRIM(STRTRAN(laFLdName[i,1],"2"))+"1"
                REPLACE &fldn WITH lafldName[i,6] IN ryjbxx
            ELSE
	            IF AT("现任",lafldName[i,5])>0 AND ryjbxx.zwbm1<"1000"&&行政人员，现任职务
	                v_zwbm=ryjbxx.zjbm1
	            ELSE
	                v_zwbm=IIF(ryjbxx.zwbm1<"1000",ryjbxx.zwbm1,"10"+RIGHT(ryjbxx.zwbm1,2))
	            ENDIF
	            SELECT bz06_jbt
	            IF ryjbxx.jbtbz>="200901"&& and INLIST(ALLTRIM(UPPER(laFLdName[i,1])),"DFBT2","BLFB2")
        		    LOCATE FOR BETWEEN(ryjbxx.gznx,worklower,workupper) AND UPPER(item)=ALLTRIM(UPPER(laFLdName[i,1])) AND (zwbm=v_zwbm OR EMPTY(zwbm)) AND tbnd=ryjbxx.jbtbz1 AND jxlb=jxlb(ryjbxx.dwbm)
        		ELSE
        		    LOCATE FOR BETWEEN(ryjbxx.gznx,worklower,workupper) AND UPPER(item)=ALLTRIM(UPPER(laFLdName[i,1])) AND (zwbm=v_zwbm OR EMPTY(zwbm)) AND tbnd=ryjbxx.jbtbz1
        		ENDIF
	            IF !EMPTY(oldalias)
	                SELECT (oldalias)
	            ENDIF
			    IF FOUND("bz06_jbt")
			        fldn=ALLTRIM(STRTRAN(laFLdName[i,1],"2"))+"1"
	                REPLACE &fldn WITH bz06_jbt.bz IN ryjbxx
		        ENDIF
		    ENDIF
        ELSE
            IF AT("手工",laFLdName[i,2])>0
                fldn=ALLTRIM(STRTRAN(laFLdName[i,1],"2"))+"1"
                REPLACE &fldn WITH m. &laFLdName[i,1] IN ryjbxx
            ENDIF
        ENDIF
    ENDFOR

	&&警衔津贴计算
    IF INLIST(LEFT(v_zwbm,2),'01','02','03')
        REPLACE jxjt1 WITH jxjt(ryjbxx.jxjtbz1,ryjbxx.jx1)+jcjt(ryjbxx.jcjtbz1,ryjbxx.jx1)+spjt(ryjbxx.spjtbz1,ryjbxx.jx1) IN ryjbxx
    ELSE
        REPLACE jxjt1 WITH 0 IN ryjbxx
    ENDIF
	IF AT("警",ryjbxx.jx1)<=0
	    REPLACE ryjbxx.jxjtbz1 WITH "" IN ryjbxx
	ENDIF
	IF AT("法",ryjbxx.jx1)<=0
	    REPLACE ryjbxx.spjtbz1 WITH "" IN ryjbxx
	ENDIF
	IF AT("检",ryjbxx.jx1)<=0
	    REPLACE ryjbxx.jcjtbz1 WITH "" IN ryjbxx
	ENDIF

	&&教护龄津贴计算
    IF LEFT(v_zwbm,2)>="07"
&&   	    REPLACE jhljt WITH m.jhljt IN ryjbxx
        REPLACE ryjbxx.jhljt1 WITH jhljt(ryjbxx.jhlqsny,ryjbxx.zdjhlnx,ryjbxx.jsnf,v_zwbm) IN ryjbxx&&重算
    ELSE
        REPLACE ryjbxx.jhljt1 WITH 0 IN ryjbxx
    ENDIF

    njbtbz=njbtbz(ryjbxx.dwbm)
    IF njbtbz>0
        REPLACE ryjbxx.njbt1 WITH njbt(ryjbxx.jbtbz1,njbtbz) IN ryjbxx
    ELSE
        REPLACE ryjbxx.njbt1 WITH 0 IN ryjbxx
    ENDIF

ENDIF

&&保留福补计算
REPLACE blfb1 WITH blfb(ryjbxx.zwbm1) IN ryjbxx

************
REPLACE ryjbxx.tgblbf WITH m.ytgbl IN ryjbxx
IF Dfbt(ryjbxx.dwbm)=0
     REPLACE ryjbxx.dfbt1 WITH 0 IN ryjbxx
ENDIF
    
m.hjgz=0
FOR i=1 TO ALEN(laFldName,1)
    fldn=ALLTRIM(STRTRAN(laFldName[i,1],"2"))+"1"
    m.hjgz=m.hjgz+ryjbxx. &fldn
ENDFOR

REPLACE hj1 with zround(m.hjgz) in ryjbxx

SELECT ryjbxx
