&&注意：一定要核对变动前后合计工资的公式，避免增资额错误！！

&&工资历次变动浏览时修改用,修改hisbase

&&取工资项目
oldalias=ALIAS()
SELECT field_name,lrfs,gld,jxryff,qsff,gdz,jbt FROM fldgz WHERE sfsy06="√" AND field_type="N" AND UPPER(field_name)<>"HJ2" ORDER BY sequence INTO ARRAY laFldName

SELECT (oldalias)

***** 计算花名册右边
IF EMPTY(zwgw2)
    RETURN
ENDIF
v_zwbm=zwbm2
IF EMPTY(v_zwbm)
    RETURN
ENDIF

v_tbnd=tbnd

&&见习人员工资
IF AT("F",v_zwbm)>0
    REPLACE zwgzse2 WITH 0,jbgzse2 WITH 0,jsdjgz2 WITH 0,jsfszwtg2 WITH 0,fdgz2 WITH 0,jxjt WITH 0,qtbt WITH 0,gwjt2 WITH 0,tgblbf WITH 0

*!*	    v_xl=ALLTRIM(SUBSTR(xl(dwbm+grbm,STRTRAN(cjgzny,".")),9))

    v_xl=ALLTRIM(SUBSTR(xl(dwbm+grbm,STRTRAN(jsnf+jsyf,".")),9))

    IF cyxx.jxgz=1
        REPLACE jxgz WITH jxgz06(v_tbnd,zwbm2,v_xl,cjgzny)
    ELSE
        REPLACE jxgz WITH m.jxgz
    ENDIF

   	IF LEFT(v_zwbm,2)>="07" AND LEFT(v_zwbm,2)<"20" AND tgbl>0 AND BITTEST(cyxx.zzrs,0)&&见习工资提高
   	    IF tbnd>='201807'
   	        IF (m.jxlb<>2 OR LEFT(zwbm2,2)<>'10')&&非义务学校或义务学校的非专技，下边20191222改，只能是教师身份，经济，会计等职位不算

*!*	   	        IF (m.jxlb<>2 OR !INLIST(xrzw,'中学高级教师','中学一级教师','中学二级教师','中学三级教师','小学高级教师','小学一级教师','小学二级教师','小学三级教师','中小学正高级教师','中小学高级教师','中小学一级教师','中小学二级教师','中小学三级教师'))&&非义务学校或义务学校的非专技
		     	REPLACE jsfszwtg2 WITH zround(jxgz*tgbl/100)
		    ELSE
		     	REPLACE jsfszwtg2 WITH zround(jxgz*(tgbl-10)/100)
            ENDIF
        ELSE
   	       	REPLACE jsfszwtg2 WITH zround(jxgz*tgbl/100)
   	    ENDIF
   	ELSE
   	   	REPLACE jsfszwtg2 WITH 0
   	ENDIF
   	
   	IF !EMPTY(v_zwbm) AND (LEFT(v_zwbm,2)<"07" OR LEFT(v_zwbm,2)>"20")&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
       	REPLACE tgbl WITH 0
    ENDIF
   	
	&&津补贴计算
    IF LEFT(v_zwbm,2)>="07" AND LEFT(v_zwbm,2)<"20"
        REPLACE jhljt WITH jhljt(jhlqsny,zdjhlnx,jsnf,v_zwbm)&&重算
    ELSE
        REPLACE jhljt WITH 0
    ENDIF

	IF (Dfbt(dwbm)=0 AND jzgb<>"是")  OR (jzgb="否" AND !EMPTY(jzgb))
	     REPLACE dfbt2 WITH 0,sdbt WITH 0
	ELSE
	    REPLACE dfbt2 WITH zround(jcjx(dwbm,LEFT(v_zwbm,3)+xlcc(v_xl),jbtbz)),sdbt WITH 0
	ENDIF
    	    		
    njbtbz=njbtbz(dwbm)
    IF njbtbz>0
        REPLACE njbt WITH njbt(jbtbz,njbtbz)
    ELSE
        REPLACE njbt WITH 0
    ENDIF

ELSE &&转正人员工资
	IF EMPTY(zwgzdc2)
	    RETURN
	ENDIF
	
	DO CASE
	CASE LEFT(zwbm2,2)="03"
		REPLACE zwgzse2 with zwgz06_fj(zwbm2,zwgzdc2,tbnd)
		REPLACE jbgzse2 WITH 0
		REPLACE jsdjgz2 WITH 0
	CASE LEFT(zwbm2,2)="21" OR LEFT(zwbm2,2)="22"
		REPLACE zwgzse2 with zwgz06(zwbm2,tbnd)
		REPLACE jbgzse2 WITH djgz06(jbgzjb2,ALLTRIM(STR(VAL(zwgzdc2)+VAL(djc2))),tbnd)
		REPLACE jsdjgz2 WITH 0
	OTHERWISE
	
		IF m.jxlb=2 AND  LEFT(zwbm2,2)='10' AND tbnd>='201807'&&义务学校、专技、2018调标
*!*			IF m.jxlb=2 AND INLIST(xrzw,'中学高级教师','中学一级教师','中学二级教师','中学三级教师','小学高级教师','小学一级教师','小学二级教师','小学三级教师','中小学正高级教师','中小学高级教师','中小学一级教师','中小学二级教师','中小学三级教师') AND tbnd='201807'&&义务学校、专技、2018调标
			REPLACE zwgzse2 with zwgz06_gr(zwbm2,zwgzdc2,djc2,tbnd)+zwgz06("11"+SUBSTR(zwbm2,3,2),tbnd)
			REPLACE jbgzse2 WITH IIF(INLIST(LEFT(zwbm2,2),"01","02","04","23","24","25","26","27","28"),jbgz06(jbgzjb2,ALLTRIM(STR(VAL(zwgzdc2)+VAL(djc2))),tbnd),0)+xjgz06(zwgzdc2,djc2,tbnd,"11"+SUBSTR(zwbm2,3,2))
		ELSE
			REPLACE zwgzse2 with zwgz06_gr(zwbm2,zwgzdc2,djc2,tbnd)+zwgz06(zwbm2,tbnd)
			REPLACE jbgzse2 WITH IIF(INLIST(LEFT(zwbm2,2),"01","02","04","23","24","25","26","27","28"),jbgz06(jbgzjb2,ALLTRIM(STR(VAL(zwgzdc2)+VAL(djc2))),tbnd),0)+xjgz06(zwgzdc2,djc2,tbnd,zwbm2)
		ENDIF		
		REPLACE jsdjgz2 WITH jsdjgz06(zwbm2,tbnd)
	ENDCASE
	
			
	**计算奖金结余
	IF !EMPTY(fddc)
	    REPLACE fdgz2 WITH fdgz06(tbnd,zwbm2,zwgzdc2,fddc)
    ENDIF
    
    IF !EMPTY(v_zwbm) AND LEFT(v_zwbm,2)<"07"&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
       	REPLACE tgbl WITH 0
    ENDIF

   	IF LEFT(zwbm2,2)>="07" AND LEFT(v_zwbm,2)<"20" AND tgbl>0
   	    IF tbnd>='201807'
   	        IF (m.jxlb<>2 OR LEFT(zwbm2,2)<>'10')&&非义务学校或义务学校的非专技，下边20191222改，只能是教师身份，经济，会计等职位不算,下边的判断暂时不可用，现任职务录入不规范

*!*	   	        IF (m.jxlb<>2 OR !INLIST(xrzw,'中学高级教师','中学一级教师','中学二级教师','中学三级教师','小学高级教师','小学一级教师','小学二级教师','小学三级教师','中小学正高级教师','中小学高级教师','中小学一级教师','中小学二级教师','中小学三级教师'))&&非义务学校或义务学校的非专技
	     	    REPLACE jsfszwtg2 WITH zround((zwgzse2+jbgzse2)*tgbl/100)
	     	ELSE
	     	    REPLACE jsfszwtg2 WITH zround((zwgzse2+jbgzse2)*(tgbl-10)/100)
	     	ENDIF
	    ELSE
	     	REPLACE jsfszwtg2 WITH zround((zwgzse2+jbgzse2)*tgbl/100)
		ENDIF	    
    ELSE
        REPLACE jsfszwtg2 WITH 0
    ENDIF
    
	&&津补贴计算
	IF (Dfbt(dwbm)=0 AND jzgb<>"是")  OR (jzgb="否" AND !EMPTY(jzgb))
	     REPLACE dfbt2 WITH 0,sdbt WITH 0
	ELSE
        IF INLIST(LEFT(zwbm2,2),'07','08','09','10','11')
            REPLACE dfbt2 WITH zround(jcjx(dwbm,v_zwbm,jbtbz)),sdbt WITH 0
        ELSE
            IF jslb='见习工资' OR tc='新增见习'
                REPLACE dfbt2 WITH zround(jcjx(dwbm,v_zwbm,jbtbz)),sdbt WITH 0
            ELSE
                REPLACE dfbt2 WITH zround(jcjx(dwbm,v_zwbm,jbtbz)),sdbt WITH zround(sdbt(dwbm,v_zwbm,jbtbz))
            ENDIF
        ENDIF
    ENDIF
	&&警衔津贴计算
	IF AT("警",jx)<=0
	    REPLACE jxjtbz WITH ""
	ENDIF
	IF AT("法",jx)<=0
	    REPLACE spjtbz WITH ""
	ENDIF
	IF AT("检",jx)<=0
	    REPLACE jcjtbz WITH ""
	ENDIF
    IF INLIST(LEFT(v_zwbm,2),'01','02','03','04','21','22','23','24','25',"26","27","28")
        REPLACE jxjt WITH jxjt(jxjtbz,jx)+jcjt(jcjtbz,jx)+spjt(spjtbz,jx)
    ELSE
        REPLACE jxjt WITH 0
    ENDIF
    
    IF LEFT(v_zwbm,2)>="07" AND LEFT(v_zwbm,2)<"20"
        REPLACE jhljt WITH jhljt(jhlqsny,zdjhlnx,jsnf,v_zwbm)&&重算,根据变动当年
    ELSE
        REPLACE jhljt WITH 0
    ENDIF
    
    njbtbz=njbtbz(dwbm)
    IF njbtbz>0
        REPLACE njbt WITH njbt(jbtbz,njbtbz)
    ELSE
        REPLACE njbt WITH 0
    ENDIF

ENDIF

&&保留福补计算
REPLACE blfb2 WITH blfb(zwbm2)

*!*	REPLACE hj2 WITH zround(zwgzse2+jbgzse2+jxgz+jsdjgz2+jsfszwtg2+fdgz2+dfbt2+blfb2+jjjy2+jxjt+qtbt+gwjt2+jhljt+tgblbf+njbt)

m.hjgz=0
FOR i=1 TO ALEN(laFldName,1)
    fn=ALLTRIM(laFldName[i,1])
    m.hjgz=m.hjgz +  &fn
ENDFOR

REPLACE hj2 with zround(m.hjgz)

*!*	IF (jslb='调入定资' OR jsnf+"."+jsyf<jrny) AND !EMPTY(zwbm1)
*!*	    RETURN
*!*	ENDIF

***** 计算花名册左边
IF jslb="2006套改"
	SELECT field_name,lrfs,gld,jxryff,qsff,gdz,jbt FROM fldgz WHERE sfsy="√" AND field_type="N" AND UPPER(field_name)<>"TGBL" AND UPPER(field_name)<>"NJBT" AND UPPER(field_name)<>"HJ2" AND (category=IIF(INLIST(v_zwbm,"01","02","03","05","06"),"01","10") OR category="00") ORDER BY sequence INTO ARRAY laFldName
	    
	m.hjgz=0
	FOR i=1 TO ALEN(laFldName,1)
		fn=ALLTRIM(STRTRAN(laFldName[i,1],"2"))+"1"
	    m.hjgz=m.hjgz+&fn
	ENDFOR

ELSE

	v_zwbm=zwbm1
	 
*!*		IF EMPTY(v_zwbm)
*!*		    RETURN
*!*		ENDIF

	v_tbnd=tbnd1

	&&见习人员工资
	IF AT("F",v_zwbm)>0

	    REPLACE zwgzse1 WITH 0,jbgzse1 WITH 0,jsdjgz1 WITH 0,jsfszwtg1 WITH 0,blfb1 WITH 0,jjjy1 WITH 0,qtbt1 WITH 0,gwjt1 WITH 0,tgblbf1 WITH 0

		&&见习期间变动前
		IF !EMPTY(zwbm1)
*!*			    v_xl=ALLTRIM(SUBSTR(xl(dwbm+grbm,STRTRAN(cjgzny,".")),9))
		    v_xl=ALLTRIM(SUBSTR(xl(dwbm+grbm,STRTRAN(jsnf+jsyf,".")),9))
		    IF cyxx.jxgz=1
		        REPLACE jxgz1 WITH jxgz06(v_tbnd,zwbm1,v_xl,cjgzny)
		    ENDIF

		   	IF LEFT(v_zwbm,2)>="07" AND LEFT(v_zwbm,2)<"20" AND tgbl1>0 AND BITTEST(cyxx.zzrs,0)&&见习工资提高
		   	    IF tbnd1>='201807'
		   	        IF (m.jxlb<>2 OR LEFT(zwbm2,2)<>'10')&&非义务学校或义务学校的非专技，下边20191222改，只能是教师身份，经济，会计等职位不算

*!*			   	        IF (m.jxlb<>2 OR !INLIST(xrzw,'中学高级教师','中学一级教师','中学二级教师','中学三级教师','小学高级教师','小学一级教师','小学二级教师','小学三级教师','中小学正高级教师','中小学高级教师','中小学一级教师','中小学二级教师','中小学三级教师'))&&非义务学校或义务学校的非专技
				     	REPLACE jsfszwtg1 WITH zround(jxgz1*tgbl1/100)
				    ELSE
				     	REPLACE jsfszwtg1 WITH zround(jxgz1*(tgbl1-10)/100)
		            ENDIF
		        ELSE
		   	       	REPLACE jsfszwtg1 WITH zround(jxgz1*tgbl1/100)
		   	    ENDIF
		   	ELSE
		   	   	REPLACE jsfszwtg1 WITH 0
		   	ENDIF
	   	
		   	IF !EMPTY(v_zwbm) AND (LEFT(v_zwbm,2)<"07" OR LEFT(v_zwbm,2)>"20")&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
		       	REPLACE tgbl1 WITH 0
		    ENDIF

	        REPLACE jhljt1 WITH 0

			IF (Dfbt(dwbm)=0 AND jzgb<>"是") OR (jzgb="否" AND !EMPTY(jzgb))
			     REPLACE dfbt1 WITH 0,sdbt1 WITH 0
			ELSE
		        REPLACE dfbt1 WITH zround(jcjx(dwbm,LEFT(v_zwbm,3)+xlcc(v_xl),jbtbz1)),sdbt1 WITH 0
		    ENDIF
	    
		    njbtbz=njbtbz(dwbm)
		    IF njbtbz>0
		        REPLACE njbt1 WITH njbt(jbtbz1,njbtbz)
		    ELSE
		        REPLACE njbt1 WITH 0
		    ENDIF
        ENDIF

	ELSE &&转正人员工资
*!*			IF EMPTY(zwgzdc1)
*!*			    RETURN
*!*			ENDIF
		
		DO case
		CASE LEFT(zwbm1,2)="03"
			REPLACE zwgzse1 with zwgz06_fj(zwbm1,zwgzdc1,tbnd1)
			REPLACE jbgzse1 WITH 0
			REPLACE jsdjgz1 WITH 0,jxgz1 WITH 0
		CASE LEFT(zwbm1,2)="21" OR LEFT(zwbm1,2)="22"
			REPLACE zwgzse1 with zwgz06(zwbm1,tbnd1)
			REPLACE jbgzse1 WITH djgz06(jbgzjb1,ALLTRIM(STR(VAL(zwgzdc1)+VAL(djc1))),tbnd1)
			REPLACE jsdjgz1 WITH 0
		OTHERWISE
			IF m.jxlb=2 AND LEFT(zwbm1,2)='10' AND tbnd1>='201807'&&义务学校、专技、2018调标
*!*				IF m.jxlb=2 AND INLIST(xrzw,'中学高级教师','中学一级教师','中学二级教师','中学三级教师','小学高级教师','小学一级教师','小学二级教师','小学三级教师','中小学正高级教师','中小学高级教师','中小学一级教师','中小学二级教师','中小学三级教师') AND tbnd1='201807'&&义务学校、专技、2018调标
				REPLACE zwgzse1 with zwgz06("11"+SUBSTR(zwbm1,3,2),tbnd1)
				REPLACE jbgzse1 WITH IIF(INLIST(LEFT(zwbm1,2),"01","02",'04',"23","24","25","26","27","28","29"),jbgz06(jbgzjb1,ALLTRIM(STR(VAL(zwgzdc1)+VAL(djc1))),tbnd1),0)+xjgz06(zwgzdc1,djc1,tbnd1,"11"+SUBSTR(zwbm1,3,2))
			ELSE
				REPLACE zwgzse1 with zwgz06_gr(zwbm1,zwgzdc1,djc1,tbnd1)+zwgz06(zwbm1,tbnd1)
				REPLACE jbgzse1 WITH IIF(INLIST(LEFT(zwbm1,2),"01","02",'04',"23","24","25","26","27","28","29"),jbgz06(jbgzjb1,ALLTRIM(STR(VAL(zwgzdc1)+VAL(djc1))),tbnd1),0)+xjgz06(zwgzdc1,djc1,tbnd1,zwbm1)
			ENDIF			
			REPLACE jsdjgz1 WITH jsdjgz06(zwbm1,tbnd1) ,jxgz1 WITH 0
        ENDCASE
			
		IF !EMPTY(fddc)
		    REPLACE fdgz1 WITH fdgz06(tbnd1,zwbm1,zwgzdc1,fddc1)
	    ENDIF
	    
	    IF !EMPTY(v_zwbm) AND LEFT(v_zwbm,2)<"07"&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
	       	REPLACE tgbl1 WITH 0
	    ENDIF

	   	IF LEFT(zwbm1,2)>="07" AND LEFT(v_zwbm,2)<"20" AND tgbl1>0
	   	    IF tbnd1>='201807'
	   	        IF (m.jxlb<>2 OR LEFT(zwbm1,2)<>'10')&&非义务学校或义务学校的非专技，下边20191222改，只能是教师身份，经济，会计等职位不算

*!*		   	        IF (m.jxlb<>2 OR !INLIST(xrzw,'中学高级教师','中学一级教师','中学二级教师','中学三级教师','小学高级教师','小学一级教师','小学二级教师','小学三级教师','中小学正高级教师','中小学高级教师','中小学一级教师','中小学二级教师','中小学三级教师'))&&非义务学校或义务学校的非专技
		     	    REPLACE jsfszwtg1 WITH zround((zwgzse1+jbgzse1)*tgbl1/100)
		     	ELSE
		     	    REPLACE jsfszwtg1 WITH zround((zwgzse1+jbgzse1)*(tgbl1-10)/100)
                ENDIF		     	
	     	ELSE
		     	REPLACE jsfszwtg1 WITH zround((zwgzse1+jbgzse1)*tgbl1/100)
		    ENDIF
	    ELSE
	     	REPLACE jsfszwtg1 WITH 0
	    ENDIF
	    
		&&津补贴计算
		IF (Dfbt(dwbm)=0 AND jzgb<>"是")  OR (jzgb="否" AND !EMPTY(jzgb))
		    REPLACE dfbt1 WITH 0,sdbt1 WITH 0
		ELSE
	        IF INLIST(LEFT(zwbm1,2),'07','08','09','10','11')
	            REPLACE dfbt1 WITH zround(jcjx(dwbm,zwbm1,jbtbz1)),sdbt1 WITH 0
	        ELSE
	            IF jslb='见习工资' OR tc='新增见习'
	                REPLACE dfbt1 WITH zround(jcjx(dwbm,zwbm1,jbtbz1)),sdbt1 WITH 0
	            ELSE
	                REPLACE dfbt1 WITH zround(jcjx(dwbm,zwbm1,jbtbz1)),sdbt1 WITH zround(sdbt(dwbm,zwbm1,jbtbz1))
	            ENDIF
	        ENDIF
        ENDIF
        
		&&警衔津贴计算
		IF AT("警",jx1)<=0
		    REPLACE jxjtbz1 WITH ""
		ENDIF
		IF AT("法",jx1)<=0
		    REPLACE spjtbz1 WITH ""
		ENDIF
		IF AT("检",jx1)<=0
		    REPLACE jcjtbz1 WITH ""
		ENDIF
	    IF INLIST(LEFT(v_zwbm,2),'01','02','03','04','21','22','23','24','25',"26","27","28")
	        REPLACE jxjt1 WITH jxjt(jxjtbz1,jx1)+jcjt(jcjtbz1,jx1)+spjt(spjtbz1,jx1)
	    ELSE
	        REPLACE jxjt1 WITH 0
	    ENDIF
	    
    	&&教护龄津贴计算, 修改变动信息时，不能修改变动前的教护龄津贴，不然和以前的不能保持一致，若要重新计算，只能推算，这样才能保持前后一致性
	    IF LEFT(zwbm1,2)>="07" AND LEFT(zwbm1,2)<"20"
	        REPLACE jhljt1 WITH jhljt(jhlqsny,zdjhlnx,jsnf,zwbm1)&&重算,
	    ELSE
	        REPLACE jhljt1 WITH 0
	    ENDIF

	    njbtbz=njbtbz(dwbm)
	    IF njbtbz>0
	        REPLACE njbt1 WITH njbt(jbtbz1,njbtbz)
	    ELSE
	        REPLACE njbt1 WITH 0
	    ENDIF

	ENDIF

	&&保留福补计算
	REPLACE blfb1 WITH blfb(zwbm1)

	m.hjgz=0
	FOR i=1 TO ALEN(laFldName,1)
	    fn=ALLTRIM(STRTRAN(laFldName[i,1],"2"))+"1"
	    m.hjgz=m.hjgz +  &fn
	ENDFOR

ENDIF
  
************    
*!*	REPLACE hj1 WITH zround(zwgzse1+jbgzse1+jsdjgz1+jxgz1+jsfszwtg1+fdgz1+dfbt1+blfb1+jjjy1+jxjt1+qtbt1+gwjt1+jhljt1+tgblbf1+njbt1)


REPLACE hj1 WITH zround(m.hjgz)
