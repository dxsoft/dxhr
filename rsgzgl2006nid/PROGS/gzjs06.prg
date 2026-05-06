PARAMETERS tname

IF PCOUNT()<=0
    tname='ryjbxx'
ENDIF
***** 计算花名册右边，ryjbxx

SELECT field_name,lrfs,gld,jxryff,qsff,gdz,jbt FROM fldgz WHERE sfsy06="√" AND field_type="N" AND UPPER(field_name)<>"HJ2" ORDER BY sequence INTO ARRAY laFldName

m.yjxjt=&tname .jxjt

FOR i=1 TO ALEN(laFLdName,1)
    IF AT("手工",laFLdName[i,2])>0&&保存手工录入值
        m .&laFldName[i,1]=&tname . &laFldname[i,1]
    ENDIF
    
    IF UPPER(laFLdName[i,1])="PGBC"
        m.pgbclrfs=laFLdName[i,2]
    ENDIF
ENDFOR

SELECT &tname 

m.jjjy=&tname .jjjy2
m.ytgbl=&tname .tgblbf
m.jxgz=&tname .jxgz
m.jhljt=&tname .jhljt
m.tgbl=&tname .tgbl
m.pgbc=&tname .pgbc&&工改保留

v_zwgzse1 = &tname .zwgzse2
v_dfbt1= &tname .dfbt2
v_sdbt1 = &tname .sdbt
v_blfb1 = &tname .blfb2

*!*	m.jjjy=jjjy06(&tname .Zwbm2,&tname .Gznx,&tname .Cjgzny,&tname .Zdgznx,&tname .dwbm,&tname .grbm,&tname .dwsx)

SELECT field_name FROM fldgz WHERE field_type="N" AND UPPER(field_name)<>"TGBL" INTO ARRAY laFldName1
v_reccnt=_tally
FOR i=1 TO v_reccnt&&工资清零
    REPLACE &tname . &laFldName1[i,1] WITH 0 IN &tname 
ENDFOR
RELEASE laFldname1

IF EMPTY(&tname .zwgw2)
    RETURN
ENDIF

v_zwbm=&tname .zwbm2
 
IF EMPTY(v_zwbm)
    RETURN
ENDIF

v_tbnd=&tname .tbnd

IF zxzc='bz'
	*!*	&&见习人员工资
	IF AT("F",v_zwbm)>0

	*!*	    v_xl=ALLTRIM(SUBSTR(xl(&tname .dwbm+&tname .grbm,STRTRAN(&tname .cjgzny,".")),9))

	    &&计算见习工资，按见习工资执行时间时的学历，2024.09.18修改
	    v_xl=ALLTRIM(SUBSTR(xl(&tname .dwbm+&tname .grbm,&tname .jsnf+&tname .jsyf),9))

	    IF cyxx.jxgz=1
	        REPLACE &tname .jxgz WITH jxgz06(v_tbnd,&tname .zwbm2,v_xl) IN &tname 
	    ELSE
	        REPLACE &tname .jxgz WITH m.jxgz IN &tname 
	    ENDIF

	   	IF LEFT(v_zwbm,2)>="07" AND &tname .tgbl>0 AND BITTEST(cyxx.zzrs,0)&&见习工资提高
	   	    IF &tname .tbnd>='201807'
	   	        IF (m.jxlb<>2 OR LEFT(&tname .zwbm2,2)<>'10')&&非义务学校或义务学校的非专技，下边20191222改，只能是教师身份，经济，会计等职位不算
	*!*	   	        IF (m.jxlb<>2 OR !INLIST(&tname .xrzw,'中学高级教师','中学一级教师','中学二级教师','中学三级教师','小学高级教师','小学一级教师','小学二级教师','小学三级教师','中小学正高级教师','中小学高级教师','中小学一级教师','中小学二级教师','中小学三级教师'))&&非义务学校或义务学校的非专技
			     	REPLACE jsfszwtg2 WITH zround(&tname .jxgz*&tname .tgbl/100) IN &tname 
			    ELSE
			     	REPLACE jsfszwtg2 WITH zround(&tname .jxgz*(&tname .tgbl-10)/100) IN &tname 
	            ENDIF
	        ELSE
	   	       	REPLACE jsfszwtg2 WITH zround(&tname .jxgz*&tname .tgbl/100) IN &tname 
	   	    ENDIF
	   	ELSE
	   	   	REPLACE jsfszwtg2 WITH 0 IN &tname 
	   	ENDIF
	   	
	   	IF !EMPTY(v_zwbm) AND LEFT(v_zwbm,2)<"07"&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
	       	REPLACE tgbl WITH 0 IN &tname 
	    ENDIF
	   	
		&&津补贴计算
	    FOR i=1 TO ALEN(laFLdName,1)
	        IF AT("自动",laFLdName[i,2])>0 AND CAST(laFldName[i,7] as i)=1 AND laFldName[i,4]=1&&见习人员发放
	            oldalias=ALIAS()
	*!*	            IF lafldName[i,5]="统一值"
	*!*	                REPLACE &laFLdName[i,1] WITH lafldName[i,6] IN &tname 
	*!*	            ELSE
	*!*	                v_zwbm=IIF(&tname .zwbm2<"1000",&tname .zwbm2,"10"+RIGHT(&tname .zwbm2,2))
	*!*		            SELECT bz06_jbt
	*!*		            IF ALLTRIM(UPPER(laFLdName[i,1]))="JHLJT"
	*!*		    		    LOCATE FOR BETWEEN(VAL(LEFT(m.rq,4))-VAL(LEFT(&tname .jhlqsny,4)),worklower,workupper) AND UPPER(item)=ALLTRIM(UPPER(laFLdName[i,1])) AND (zwbm=v_zwbm OR EMPTY(zwbm)) AND tbnd=&tname .jbtbz
	*!*		    		ELSE
	*!*			            IF &tname .jbtbz>="200901"&& and INLIST(ALLTRIM(UPPER(laFLdName[i,1])),"DFBT2","BLFB2")
	*!*	    	    		    LOCATE FOR BETWEEN(&tname .gznx,worklower,workupper) AND UPPER(item)=ALLTRIM(UPPER(laFLdName[i,1])) AND (zwbm=LEFT(v_zwbm,3)+xlcc(v_xl) OR zwbm=v_zwbm OR EMPTY(zwbm)) AND tbnd=&tname .jbtbz AND jxlb=jxlb(&tname .dwbm)
	*!*	    	    		ELSE
	*!*	    	    		    LOCATE FOR BETWEEN(&tname .gznx,worklower,workupper) AND UPPER(item)=ALLTRIM(UPPER(laFLdName[i,1])) AND (LEFT(zwbm,3)=LEFT(v_zwbm,3) OR EMPTY(zwbm)) AND tbnd=&tname .jbtbz
	*!*	    	    		ENDIF
	*!*		    		ENDIF
	*!*		            IF !EMPTY(oldalias)
	*!*		                SELECT (oldalias)
	*!*		            ENDIF
	*!*				    IF FOUND("bz06_jbt")
	*!*		                REPLACE &laFLdName[i,1] WITH bz06_jbt.bz IN &tname 
	*!*			        ENDIF
	*!*			    ENDIF
	        ELSE
	            IF AT("手工",laFLdName[i,2])>0
	                REPLACE &laFLdName[i,1] WITH m. &laFLdName[i,1] IN &tname 
	            ENDIF
	        ENDIF
	    ENDFOR


	    &&重算教护龄应该根据变动年份计算，之前依m.rq，重新计算时，所有教护龄都算到当前值了。2016.11.30改。  考虑见习时间长的问题,根据从教时间确定是否有津贴
	    IF LEFT(v_zwbm,2)>="07" AND LEFT(v_zwbm,2)<"20" AND !EMPTY(STRTRAN(&tname .jhlqsny,"."))
	        REPLACE &tname .jhljt WITH jhljt(&tname .jhlqsny,&tname .zdjhlnx,&tname .jsnf,v_zwbm) IN &tname 
	    ELSE
	        REPLACE &tname .jhljt WITH 0 IN &tname 
	    ENDIF

		&&津补贴绩效
		IF (Dfbt(&tname .dwbm)=0 AND &tname .jzgb<>"是")  OR (&tname .jzgb="否" AND !EMPTY(&tname .jzgb) AND &tname .spdw='市委组织部')
		    REPLACE dfbt2 WITH 0,sdbt WITH 0 IN &tname 
		ELSE
	        REPLACE dfbt2 WITH zround(jcjx(&tname .dwbm,LEFT(v_zwbm,3)+xlcc(v_xl),&tname .jbtbz)) IN &tname 
	    ENDIF
	    
	    
		&&保留福补计算
	    REPLACE blfb2 WITH blfb(&tname .zwbm2) IN &tname 

	    njbtbz=njbtbz(&tname .dwbm)
	    IF njbtbz>0
	        REPLACE &tname .njbt WITH njbt(&tname .jbtbz,njbtbz) IN &tname 
	    ELSE
	        REPLACE &tname .njbt WITH 0 IN &tname 
	    ENDIF

	ELSE &&转正人员工资
		IF EMPTY(&tname .zwgzdc2)
		    RETURN
		ENDIF
		
		DO case
		CASE LEFT(&tname .zwbm2,2)="03"
			REPLACE zwgzse2 with zwgz06_fj(&tname .zwbm2,&tname .zwgzdc2,&tname .tbnd) IN &tname 
			REPLACE jbgzse2 WITH 0 IN &tname 
			REPLACE jsdjgz2 WITH 0 IN &tname  
		CASE LEFT(&tname .zwbm2,2)="21" OR LEFT(&tname .zwbm2,2)="22"
			REPLACE zwgzse2 with zwgz06(&tname .zwbm2,&tname .tbnd) IN &tname 
			REPLACE jbgzse2 WITH djgz06(&tname .jbgzjb2,ALLTRIM(STR(VAL(&tname .zwgzdc2)+VAL(&tname .djc2))),&tname .tbnd) IN &tname 
			REPLACE jsdjgz2 WITH 0 IN &tname  
		OTHERWISE
			IF m.jxlb=2 AND LEFT(&tname .zwbm2,2)='10' AND &tname .tbnd>='201807'&&2018年调标，义务教育，专技
			
	*!*			IF m.jxlb=2 AND &tname .tbnd='201807' AND INLIST(&tname .xrzw,'中学高级教师','中学一级教师','中学二级教师','中学三级教师','小学高级教师','小学一级教师','小学二级教师','小学三级教师','中小学正高级教师','中小学高级教师','中小学一级教师','中小学二级教师','中小学三级教师')&&&2018年调标，义务教育，教师
				REPLACE zwgzse2 with zwgz06_gr(&tname .zwbm2,&tname .zwgzdc2,&tname .djc2,&tname .tbnd)+zwgz06("11"+SUBSTR(&tname .zwbm2,3,2),&tname .tbnd) IN &tname 
				IF INLIST(LEFT(&tname .zwbm2,2),"01","02","04","23","24","25","26","27","28")
					REPLACE jbgzse2 WITH jbgz06(&tname .jbgzjb2,ALLTRIM(STR(VAL(&tname .zwgzdc2)+VAL(&tname .djc2))),&tname .tbnd) IN &tname 
				ELSE
				    IF INLIST(LEFT(&tname .zwbm2,2),"21","22")&&执法勤务警员、警务技术职务
						REPLACE jbgzse2 WITH jydjgz06(&tname .jbgzjb2,ALLTRIM(STR(VAL(&tname .zwgzdc2)+VAL(&tname .djc2))),&tname .tbnd) IN &tname 
					ELSE
					    REPLACE jbgzse2 WITH xjgz06(&tname .zwgzdc2,&tname .djc2,&tname .tbnd,"11"+SUBSTR(&tname .zwbm2,3,2)) IN &tname 
					ENDIF
			    ENDIF
			ELSE
				REPLACE zwgzse2 with zwgz06_gr(&tname .zwbm2,&tname .zwgzdc2,&tname .djc2,&tname .tbnd)+zwgz06(&tname .zwbm2,&tname .tbnd) IN &tname 
				IF INLIST(LEFT(&tname .zwbm2,2),"01","02","04","23","24","25","26","27","28")
					REPLACE jbgzse2 WITH jbgz06(&tname .jbgzjb2,ALLTRIM(STR(VAL(&tname .zwgzdc2)+VAL(&tname .djc2))),&tname .tbnd) IN &tname 
				ELSE
				    IF INLIST(LEFT(&tname .zwbm2,2),"21","22")
						REPLACE jbgzse2 WITH jydjgz06(&tname .jbgzjb2,ALLTRIM(STR(VAL(&tname .zwgzdc2)+VAL(&tname .djc2))),&tname .tbnd) IN &tname 
					ELSE
						REPLACE jbgzse2 WITH xjgz06(&tname .zwgzdc2,&tname .djc2,&tname .tbnd,&tname .zwbm2) IN &tname 
					ENDIF
				ENDIF
	        ENDIF		
			REPLACE jsdjgz2 WITH jsdjgz06(&tname .zwbm2,&tname .tbnd) IN &tname  
		ENDCASE
		
		**计算奖金结余
	    
	    REPLACE jjjy2 WITH m.jjjy IN &tname 
	    REPLACE tgbl WITH m.tgbl IN &tname 

		IF !EMPTY(&tname .fddc) AND STRTRAN(&tname .fdsj,".")<=&tname .jsnf+&tname .jsyf
	        REPLACE fdgz2 WITH fdgz06(&tname .tbnd,&tname .zwbm2,&tname .zwgzdc2,&tname .fddc) IN &tname 
	    ENDIF
	    
	    IF !EMPTY(v_zwbm) AND LEFT(v_zwbm,2)<"07"&&2010.04.01改，以前没考虑为空情况，当新增时为空，10%自动清0
	       	REPLACE tgbl WITH 0 IN &tname 
	    ENDIF

	   	IF LEFT(&tname .zwbm2,2)>="07" AND LEFT(&tname .zwbm2,2)<"20" AND &tname .tgbl>0
	   	    IF &tname .tbnd>='201807'
	   	        IF (m.jxlb<>2 OR LEFT(&tname .zwbm2,2)<>'10')&&非义务学校或义务学校的非专技，下边20191222改，只能是教师身份，经济，会计等职位不算
	*!*	   	        IF (m.jxlb<>2 OR !INLIST(&tname .xrzw,'中学高级教师','中学一级教师','中学二级教师','中学三级教师','小学高级教师','小学一级教师','小学二级教师','小学三级教师','中小学正高级教师','中小学高级教师','中小学一级教师','中小学二级教师','中小学三级教师'))&&非义务学校或义务学校的非专技
			     	REPLACE jsfszwtg2 WITH zround((&tname .zwgzse2+&tname .jbgzse2)*&tname .tgbl/100) IN &tname 
			    ELSE
			     	REPLACE jsfszwtg2 WITH zround((&tname .zwgzse2+&tname .jbgzse2)*(&tname .tgbl-10)/100) IN &tname 
	            ENDIF
	        ELSE
		     	REPLACE jsfszwtg2 WITH zround((&tname .zwgzse2+&tname .jbgzse2)*&tname .tgbl/100) IN &tname 
	        ENDIF
	    ELSE
	        REPLACE jsfszwtg2 WITH 0 IN &tname 
	    ENDIF
	    
		&&津补贴计算
	    FOR i=1 TO ALEN(laFLdName,1)
	        IF AT("自动",laFLdName[i,2])>0 AND CAST(laFldName[i,7] as i)=1&&津补贴项
	            oldalias=ALIAS()
	*!*	            IF lafldName[i,5]="统一值"
	*!*	                REPLACE &laFLdName[i,1] WITH lafldName[i,6] IN &tname 
	*!*	            ELSE
	*!*		            SELECT bz06_jbt
	*!*		            IF AT("现任",lafldName[i,5])>0 AND &tname .zwbm2<"1000"&&行政人员，现任职务
	*!*		                v_zwbm=&tname .zjbm
	*!*		            ELSE
	*!*		                v_zwbm=IIF(&tname .zwbm2<"1000",&tname .zwbm2,"10"+RIGHT(&tname .zwbm2,2))
	*!*		            ENDIF
	*!*		            IF &tname .jbtbz>="200901"&& and INLIST(ALLTRIM(UPPER(laFLdName[i,1])),"DFBT2","BLFB2")
	*!*	        		    LOCATE FOR BETWEEN(&tname .gznx,worklower,workupper) AND ALLTRIM(UPPER(item))==ALLTRIM(UPPER(laFLdName[i,1])) AND (zwbm=v_zwbm OR EMPTY(zwbm)) AND tbnd=&tname .jbtbz AND jxlb=jxlb(&tname .dwbm)
	*!*	        		ELSE
	*!*	        		    LOCATE FOR BETWEEN(&tname .gznx,worklower,workupper) AND ALLTRIM(UPPER(item))==ALLTRIM(UPPER(laFLdName[i,1])) AND (zwbm=v_zwbm OR EMPTY(zwbm)) AND tbnd=&tname .jbtbz
	*!*	        		ENDIF
	*!*		            IF !EMPTY(oldalias)
	*!*		                SELECT (oldalias)
	*!*		            ENDIF
	*!*				    IF FOUND("bz06_jbt")
	*!*		                REPLACE &laFLdName[i,1] WITH bz06_jbt.bz IN &tname 
	*!*			        ENDIF
	*!*			    ENDIF
	        ELSE
	            IF AT("手工",laFLdName[i,2])>0
	                REPLACE &laFLdName[i,1] WITH m. &laFLdName[i,1] IN &tname 
	            ENDIF
	        ENDIF
	    ENDFOR

		IF (Dfbt(&tname .dwbm)=0 AND &tname .jzgb<>"是") OR (&tname .jzgb="否" AND !EMPTY(&tname .jzgb))&&单位不批且个人也不批
		    REPLACE dfbt2 WITH 0,sdbt WITH 0 IN &tname 
		ELSE
	        IF INLIST(LEFT(&tname .zwbm2,2),'07','08','09','10','11')
	            REPLACE dfbt2 WITH zround(jcjx(&tname .dwbm,v_zwbm,&tname .jbtbz)),sdbt WITH 0 IN &tname 
	        ELSE
	            IF ryjbxx.tc='新增见习'&&&tname .jslb='见习工资' OR (&tname .sdbt=0 AND &tname .jslb='调标晋升')&&  &tname .tc='新增见习'&&后边条件用于调标晋升.20250112 hisbase去掉了tc字段，调表改成
	                REPLACE dfbt2 WITH zround(jcjx(&tname .dwbm,v_zwbm,&tname .jbtbz)),sdbt WITH 0 IN &tname 
	            ELSE
	                REPLACE dfbt2 WITH zround(jcjx(&tname .dwbm,v_zwbm,&tname .jbtbz)),sdbt WITH zround(sdbt(&tname .dwbm,v_zwbm,&tname .jbtbz)) IN &tname 
	            ENDIF
	        ENDIF
	    ENDIF
	    
		&&保留福补计算
	    REPLACE blfb2 WITH blfb(&tname .zwbm2) IN &tname 

	*!*		&&基础性绩效工资计算
	*!*		IF  m.pdwbz="行政"
	*!*		ELSE
	*!*			IF EMPTY(m.pjxbl)
	*!*	    	    REPLACE dfbt2 WITH 0 IN &tname 
	*!*	    	ELSE
	*!*	    	    v_bl=STRTRAN(m.pjxbl,"：",":")
	*!*	    	    REPLACE dfbt2 WITH zround(&tname .dfbt2*10*VAL(LEFT(v_bl,AT(":",v_bl)-1))/(VAL(LEFT(v_bl,AT(":",v_bl)-1))+VAL(SUBSTR(v_bl,AT(":",v_bl)+1)))/7) IN &tname 
	*!*	        ENDIF
	*!*	    ENDIF    	
	    
		&&警衔津贴计算,注意事业无
	    IF INLIST(LEFT(v_zwbm,2),'01','02','03','21','22','23','24','25',"26","27","28") AND !EMPTY(&tname .jx)
*!*		        REPLACE jxjt WITH jxjt(&tname .jxjtbz,&tname .jx)+jcjt(&tname .jcjtbz,&tname .jx)+spjt(&tname .spjtbz,&tname .jx) IN &tname 

	        REPLACE jxjt WITH jxjt(&tname .jxjtbz,&tname .jx) IN &tname 

	    ELSE
	        REPLACE jxjt WITH 0 IN &tname 
	    ENDIF
	    
*!*			IF AT("警",&tname .jx)<=0 AND AT("监",&tname .jx)<=0
*!*			    REPLACE &tname .jxjtbz WITH "" IN &tname 
*!*			ENDIF
*!*			IF AT("法",&tname .jx)<=0
*!*			    REPLACE &tname .spjtbz WITH "" IN &tname 
*!*			ENDIF
*!*			IF AT("检",&tname .jx)<=0
*!*			    REPLACE &tname .jcjtbz WITH "" IN &tname 
*!*			ENDIF

	    &&重算教护龄应该根据变动年份计算，之前依m.rq，重新计算时，所有教护龄都算到当前值了。2016.11.30改。
	    IF LEFT(v_zwbm,2)>="07" AND LEFT(v_zwbm,2)<"20" AND !EMPTY(STRTRAN(&tname .jhlqsny,"."))
	        REPLACE &tname .jhljt WITH jhljt(&tname .jhlqsny,&tname .zdjhlnx,&tname .jsnf,v_zwbm) IN &tname 
	    ELSE
	        REPLACE &tname .jhljt WITH 0 IN &tname 
	    ENDIF

	*!*	    &&使用的jslqsny和中断时间使用的是基本信息里的，调用此方法前确保定位基本信息正确   有后来取消的情况，不能用这种方式，还是依工资信息里的
	*!*	    IF LEFT(v_zwbm,2)>="07" AND LEFT(v_zwbm,2)<"20" AND !EMPTY(STRTRAN(ryjbxx.jhlqsny,"."))
	*!*	        REPLACE &tname .jhljt WITH jhljt(ryjbxx.jhlqsny,ryjbxx.zdjhlnx,&tname .jsnf,v_zwbm) IN &tname 
	*!*	    ELSE
	*!*	        REPLACE &tname .jhljt WITH 0 IN &tname 
	*!*	    ENDIF

	    njbtbz=njbtbz(&tname .dwbm)
	    IF njbtbz>0
	        REPLACE &tname .njbt WITH njbt(&tname .jbtbz,njbtbz) IN &tname 
	    ELSE
	        REPLACE &tname .njbt WITH 0 IN &tname 
	    ENDIF

	ENDIF

	************
	IF &tname .dwsx<'07'
	    REPLACE &tname .tgblbf WITH 0 IN &tname &&机关，去掉特岗保留部分
	ELSE
	    REPLACE &tname .tgblbf WITH m.ytgbl IN &tname 
	ENDIF

	IF Dfbt(&tname .dwbm)=0 AND &tname .jzgb<>"是"
	     REPLACE &tname .dfbt2 WITH 0,&tname .sdbt WITH 0 IN &tname 
	ENDIF

	&&  搞错了，这个是要冲销的，工改保留工资保留指的是：加班补贴+岗位津贴+审判检察津贴
	IF m.pgbc>0
	    m.zze = (&tname .zwgzse2-v_zwgzse1)+(&tname .dfbt2+&tname .sdbt-v_dfbt1-v_sdbt1)+(&tname .blfb2-v_blfb1)
	    IF (&tname .jslb='职务变化' OR &tname .jslb='职级晋升') AND ryjbxx.jrfs<>"转隶" AND m.zze>0&&检察院转隶到纪委，原工资高出部分永久保留不冲销20210422,其他单位的保留职务工资在职务晋升时冲销
			REPLACE &tname .pgbc WITH IIF(m.pgbc>m.zze,m.pgbc-m.zze,0) IN &tname 
		ELSE
			REPLACE &tname .pgbc WITH m.pgbc IN &tname 
		ENDIF
	ENDIF
	    
	m.hjgz=0
	FOR i=1 TO ALEN(laFldName,1)
	    m.hjgz=m.hjgz+&tname . &laFldName[i,1]
	ENDFOR

	REPLACE hj2 with zround(m.hjgz) in &tname 
ELSE
	&&淮滨医院
	SQLEXEC(conn,"SELECT * FROM dxl WHERE dwbm='"+&tname .dwbm+"' AND grbm='"+&tname .grbm+"' AND xllb='普通全日制' ORDER BY bysj desc","lst")
	REPLACE jcgz2 WITH jcgzbyxl(lst.xlbm) IN &tname 
	REPLACE jbgzse2 with xjgz(zwgzdc2) IN &tname 
	REPLACE zwgzse2 with gwgz(zwbm2) IN &tname 

	REPLACE dfbt2 WITH ROUND(jbgzse2*3/7,0) IN &tname 
	REPLACE jsfszwtg2 WITH ROUND((zwgzse2+jcgz2)*tgbl/100,0) IN &tname 
	REPLACE hj2 WITH jcgz2+zwgzse2+jbgzse2+dfbt2+jsfszwtg2 IN &tname 
ENDIF

SELECT &tname 
