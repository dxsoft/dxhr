
&&只计算变动右边

***** 计算花名册右边
IF EMPTY(zwgw2)
    RETURN
ENDIF
v_zwbm=zwbm2
IF EMPTY(v_zwbm)
    RETURN
ENDIF

*!*	IF dwbm="018" AND grbm="00042"
*!*	    aaaa=1
*!*	ENDIF

v_zwbm=zwbm2
v_tbnd=tbnd
m.jhljt=jhljt
m.jhljt1=jhljt1

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
    IF LEFT(v_zwbm,2)>="07"
        REPLACE jhljt WITH jhljt(jhlqsny,zdjhlnx,jsnf,v_zwbm)&&重算
    ELSE
        REPLACE jhljt WITH 0
    ENDIF

	IF Dfbt(ryjbxx.dwbm)=0
	     REPLACE dfbt2 WITH 0
	ELSE
	    REPLACE dfbt2 WITH jcjx(dwbm,v_zwbm,jbtbz)
	ENDIF
    	    		
    njbtbz=njbtbz(dwbm)
    IF njbtbz>0
        REPLACE njbt WITH njbt(jbtbz,njbtbz)
    ELSE
        REPLACE njbt WITH 0
    ENDIF

    REPLACE zwgzse2 WITH 0,jbgzse2 WITH 0,jsdjgz2 WITH 0,jsfszwtg2 WITH 0,fdgz2 WITH 0,jxjt WITH 0,qtbt WITH 0,gwjt2 WITH 0,tgblbf WITH 0

ELSE &&转正人员工资
	IF EMPTY(zwgzdc2)
	    RETURN
	ENDIF
	
	REPLACE zwgzse2 with zwgz06_gr(zwbm2,zwgzdc2,djc2,tbnd)+zwgz06(zwbm2,tbnd)
	REPLACE jbgzse2 WITH IIF(INLIST(LEFT(zwbm2,2),"01","02","03"),jbgz06(jbgzjb2,ALLTRIM(STR(VAL(zwgzdc2)+VAL(djc2))),tbnd),0)+xjgz06(zwgzdc2,djc2,tbnd,zwbm2)
	REPLACE jsdjgz2 WITH jsdjgz06(zwbm2,tbnd),jxgz WITH 0 
	
	**计算奖金结余
	IF !EMPTY(fddc)
	    REPLACE fdgz2 WITH fdgz06(tbnd,zwbm2,zwgzdc2,fddc)
    ENDIF
    
    IF !EMPTY(v_zwbm) AND LEFT(v_zwbm,2)<"07"&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
       	REPLACE tgbl WITH 0
    ENDIF

   	IF LEFT(zwbm2,2)>="07"
     	REPLACE jsfszwtg2 WITH zround((zwgzse2+jbgzse2)*tgbl/100)
    ELSE
        REPLACE jsfszwtg2 WITH 0
    ENDIF
    
	&&津补贴计算
	IF Dfbt(ryjbxx.dwbm)=0
	     REPLACE dfbt2 WITH 0
	ELSE
        REPLACE dfbt2 WITH jcjx(dwbm,v_zwbm,jbtbz)
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
    IF INLIST(LEFT(v_zwbm,2),'01','02','03')
        REPLACE jxjt WITH jxjt(jxjtbz,jx)+jcjt(jcjtbz,jx)+spjt(spjtbz,jx)
    ELSE
        REPLACE jxjt WITH 0
    ENDIF
    
	&&教护龄津贴计算, 修改变动信息时，不能修改教护龄津贴，不然和以前的不能保持一致，若要重新计算，只能推算，这样才能保持前后一致性
*!*	    IF LEFT(v_zwbm,2)>="07"
*!*	        REPLACE jhljt WITH jhljt(jhlqsny,zdjhlnx,jsnf,v_zwbm)&&重算,根据变动当年
*!*	    ELSE
*!*	        REPLACE jhljt WITH 0
*!*	    ENDIF
    REPLACE jhljt WITH m.jhljt
    
    njbtbz=njbtbz(dwbm)
    IF njbtbz>0
        REPLACE njbt WITH njbt(jbtbz,njbtbz)
    ELSE
        REPLACE njbt WITH 0
    ENDIF

ENDIF

&&保留福补计算
REPLACE blfb2 WITH blfb(zwbm2)

REPLACE hj2 WITH zround(zwgzse2+jbgzse2+jxgz+jsdjgz2+jsfszwtg2+fdgz2+dfbt2+blfb2+jjjy2+jxjt+qtbt+gwjt2+jhljt+tgblbf)
