**计算养老平均工资用
***** 计算花名册右边，与表无关
&&取工资项目
SELECT field_name,lrfs,gld,jxryff,qsff,gdz,jbt FROM fldgz WHERE sfsy06="√" AND field_type="N" AND UPPER(field_name)<>"HJ2" ORDER BY sequence INTO ARRAY laFldName

IF EMPTY(zwgw2)
    RETURN
ENDIF
v_zwbm=zwbm2
IF EMPTY(v_zwbm)
    RETURN
ENDIF

v_zwbm=zwbm2
v_tbnd=tbnd

&&见习人员工资
IF AT("F",v_zwbm)>0

    v_xl=ALLTRIM(SUBSTR(xl(dwbm+grbm,STRTRAN(cjgzny,".")),9))
    IF cyxx.jxgz=1
        REPLACE jxgz WITH jxgz06(v_tbnd,zwbm2,v_xl,cjgzny)
    ELSE
        REPLACE jxgz WITH m.jxgz
    ENDIF

   	IF LEFT(v_zwbm,2)>="07"
   	    IF BITTEST(cyxx.zzrs,0)&&见习工资提高
   	       	REPLACE jsfszwtg2 WITH zround(jxgz*tgbl/100)
   	    ELSE
   	       	REPLACE jsfszwtg2 WITH 0
   	    ENDIF
   	ENDIF
   	IF !EMPTY(v_zwbm) AND LEFT(v_zwbm,2)<"07"&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
       	REPLACE tgbl WITH 0
    ENDIF
   	
	&&津补贴计算
    &&重算教护龄应该根据变动年份计算，之前依m.rq，重新计算时，所有教护龄都算到当前值了。2016.11.30改。考虑从教时间长的问题
    IF LEFT(v_zwbm,2)>="07" AND !EMPTY(STRTRAN(jhlqsny,"."))
        REPLACE jhljt WITH jhljt(jhlqsny,zdjhlnx,jsnf,v_zwbm)
    ELSE
        REPLACE jhljt WITH 0
    ENDIF

    REPLACE dfbt2 WITH dfbtbz(LEFT(v_zwbm,3)+xlcc(v_xl),jbtbz,jxlb(dwbm))
    	    		
    njbtbz=njbtbz(dwbm)
    IF njbtbz>0
        REPLACE njbt WITH njbt(jbtbz,njbtbz)
    ELSE
        REPLACE njbt WITH 0
    ENDIF

	REPLACE zwgzse2 WITH 0,jbgzse2 WITH 0,jsdjgz2 WITH 0,jsfszwtg2 WITH 0,fdgz2 WITH 0,jxjt WITH 0,qtbt WITH 0,tgblbf WITH 0

ELSE &&转正人员工资
	IF EMPTY(zwgzdc2)
	    RETURN
	ENDIF
	
	REPLACE zwgzse2 with zwgz06_gr(zwbm2,zwgzdc2,djc2,tbnd)+zwgz06(zwbm2,tbnd)
	REPLACE jbgzse2 WITH IIF(INLIST(LEFT(zwbm2,2),"01","02","03","23","24","25","26","27","28","29"),jbgz06(jbgzjb2,ALLTRIM(STR(VAL(zwgzdc2)+VAL(djc2))),tbnd),0)+xjgz06(zwgzdc2,djc2,tbnd,zwbm2)
	REPLACE jsdjgz2 WITH jsdjgz06(zwbm2,tbnd),jxgz WITH 0 
	
	**计算奖金结余
	IF !EMPTY(fddc)
	    REPLACE fdgz2 WITH fdgz06(tbnd,zwbm2,zwgzdc2,fddc)
    ENDIF
    
    IF !EMPTY(v_zwbm) AND LEFT(v_zwbm,2)<"07"&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
       	REPLACE tgbl WITH 0
    ENDIF

   	IF LEFT(zwbm2,2)>="07" AND LEFT(zwbm2,2)<"20"
     	REPLACE jsfszwtg2 WITH zround((zwgzse2+jbgzse2)*tgbl/100)
    ELSE
        REPLACE jsfszwtg2 WITH 0
    ENDIF
    
	&&津补贴计算
    REPLACE dfbt2 WITH dfbtbz(v_zwbm,jbtbz,jxlb(dwbm))

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
    
	&&教护龄津贴计算

    &&重算教护龄应该根据变动年份计算，之前依m.rq，重新计算时，所有教护龄都算到当前值了。2016.11.30改。
    IF LEFT(v_zwbm,2)>="07" AND LEFT(v_zwbm,2)<"20" AND !EMPTY(STRTRAN(jhlqsny,"."))
        REPLACE jhljt WITH jhljt(jhlqsny,zdjhlnx,jsnf,v_zwbm)
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

IF dwsx<'07'
    REPLACE tgblbf WITH 0&&机关，去掉特岗保留部分
ENDIF

*!*	REPLACE hj2 WITH zround(zwgzse2+jbgzse2+jxgz+jsdjgz2+jsfszwtg2+fdgz2+dfbt2+blfb2+jjjy2+jxjt+qtbt+gwjt2+jhljt+tgblbf+njbt)
m.hjgz=0
FOR i=1 TO ALEN(laFldName,1)
    m.hjgz=m.hjgz + &laFldName[i,1]
ENDFOR
REPLACE hj2 with zround(m.hjgz)


***** 计算花名册左边
IF jslb="2006套改"
ELSE

	v_zwbm=zwbm1
	 
	IF EMPTY(v_zwbm)
	    RETURN
	ENDIF

	v_tbnd=tbnd1

	&&见习人员工资
	IF AT("F",v_zwbm)>0

	    v_xl=ALLTRIM(SUBSTR(xl(dwbm+grbm,STRTRAN(cjgzny,".")),9))
	    IF cyxx.jxgz=1
	        REPLACE jxgz1 WITH jxgz06(v_tbnd,zwbm1,v_xl,cjgzny)
	    ENDIF

	   	IF LEFT(v_zwbm,2)>="07" AND LEFT(v_zwbm,2)<"20"
	   	    IF BITTEST(cyxx.zzrs,0)&&见习工资提高
	   	       	REPLACE jsfszwtg1 WITH zround(jxgz1*tgbl/100)
	   	    ELSE
	   	       	REPLACE jsfszwtg1 WITH 0
	   	    ENDIF
	   	ENDIF
	   	IF !EMPTY(v_zwbm) AND LEFT(v_zwbm,2)<"07"&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
	       	REPLACE tgbl WITH 0
	    ENDIF

	    &&重算教护龄应该根据变动年份计算，之前依m.rq，重新计算时，所有教护龄都算到当前值了。2016.11.30改。考虑从教时间长的问题
	    IF LEFT(v_zwbm,2)>="07" AND LEFT(v_zwbm,2)<"20" AND !EMPTY(STRTRAN(jhlqsny,"."))
	        REPLACE jhljt1 WITH jhljt(jhlqsny,zdjhlnx,jsnf,v_zwbm)
	    ELSE
	        REPLACE jhljt1 WITH 0
	    ENDIF

	    REPLACE dfbt1 WITH dfbtbz(LEFT(v_zwbm,3)+xlcc(v_xl),jbtbz1,jxlb(dwbm))

	    njbtbz=njbtbz(dwbm)
	    IF njbtbz>0
	        REPLACE njbt1 WITH njbt(jbtbz1,njbtbz)
	    ELSE
	        REPLACE njbt1 WITH 0
	    ENDIF

		REPLACE zwgzse1 WITH 0,jbgzse1 WITH 0,jsdjgz1 WITH 0,jsfszwtg1 WITH 0,fdgz1 WITH 0,jxjt1 WITH 0,qtbt1 WITH 0,tgblbf WITH 0

	ELSE &&转正人员工资
		IF EMPTY(zwgzdc1)
		    RETURN
		ENDIF
		
		REPLACE zwgzse1 with zwgz06_gr(zwbm1,zwgzdc1,djc1,tbnd1)+zwgz06(zwbm1,tbnd1)
		REPLACE jbgzse1 WITH IIF(INLIST(LEFT(zwbm1,2),"01","02","03","23","24","25","26","27","28","29"),jbgz06(jbgzjb1,ALLTRIM(STR(VAL(zwgzdc1)+VAL(djc1))),tbnd1),0)+xjgz06(zwgzdc1,djc1,tbnd1,zwbm1)
		REPLACE jsdjgz1 WITH jsdjgz06(zwbm1,tbnd1) ,jxgz1 WITH 0
		
		IF !EMPTY(fddc)
		    REPLACE fdgz1 WITH fdgz06(tbnd1,zwbm1,zwgzdc1,fddc1)
	    ENDIF
	    
	    IF !EMPTY(v_zwbm) AND (LEFT(v_zwbm,2)<"07" OR LEFT(v_zwbm,2)>"20")&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
	       	REPLACE tgbl1 WITH 0
	    ENDIF

	   	IF LEFT(zwbm1,2)>="07" AND LEFT(zwbm1,2)<"20"
	     	REPLACE jsfszwtg1 WITH zround((zwgzse1+jbgzse1)*tgbl1/100)
	    ELSE
    	    REPLACE jsfszwtg1 WITH 0
	    ENDIF
	    
		&&津补贴计算
	    REPLACE dfbt1 WITH dfbtbz(v_zwbm,jbtbz1,jxlb(dwbm))

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
	    
       &&重算教护龄应该根据变动年份计算，之前依m.rq，重新计算时，所有教护龄都算到当前值了。2016.11.30改。
   	    IF LEFT(v_zwbm,2)>="07" AND LEFT(v_zwbm,2)<"20" AND !EMPTY(STRTRAN(jhlqsny,"."))
	        REPLACE jhljt1 WITH jhljt(jhlqsny,zdjhlnx,jsnf,v_zwbm)
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
ENDIF
  
IF dwsx<'07'
    REPLACE tgblbf1 WITH 0&&机关，去掉特岗保留部分
ENDIF

************    
*!*	REPLACE hj1 WITH zround(zwgzse1+jbgzse1+jsdjgz1+jxgz1+jsfszwtg1+fdgz1+dfbt1+blfb1+jjjy1+jxjt1+qtbt1+gwjt1+jhljt1+tgblbf1+njbt1)

m.hjgz=0
FOR i=1 TO ALEN(laFldName,1)
    fn=ALLTRIM(STRTRAN(laFldName[i,1],"2"))+"1"
    m.hjgz=m.hjgz + &fn
ENDFOR
REPLACE hj1 WITH zround(m.hjgz)

