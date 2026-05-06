FUNCTION nxj

PARAMETERS tdwbm,tgrbm,tnds,tnde,tcate

**tcate=0,计算12月份工资，即为计算年终一次性奖金提供数据
&&应发工资

LOCAL jbgz,jt,ys,ze,ki,retv,y1,m.start,m.zwbm,m.latgz

DIMENSION latgz[1]

tnf=LEFT(tnds,4)

SELECT wmdw FROM dwbm WHERE ALLTRIM(dwbm)==ALLTRIM(m.pdwbm) INTO ARRAY lat
m.wmdw=lat[1]

SELECT TOP 1 tbnd FROM bz_pskhj WHERE tbnd<=tnf+'12' ORDER BY tbnd DESC INTO ARRAY lat
m.pskhjbz=lat[1]

&&公务员和见习人员不折算dfbt，标准按时间自动计算（考虑没调津补贴的情况），hj2必须用实际的值，之前设为00000，造成排序有问题
SELECT dwbm,grbm,xm,SPACE(18) as sfzh,SPACE(7) as cjgzny,zwbm2,zjbm,dwsx,zwgw2,zwgzdc2,djc2,jbgzjb2,fddc,tgbl,jx,jslb,jsnf,jsyf,"      " as tbnd,"      " as jbtbz,"      " as jxjtbz,"      " as wytx,00000 as zwgzse2,00000 as jbgzse2,00000 as jsdjgz2,00000 as jsfszwtg2,00000 as fdgz2,00 as blfb2,jjjy2,jhljt,tgblbf,gwjt2,hj2,zgxl,tc FROM hisbase WHERE dwbm=tdwbm AND grbm=tgrbm AND jslb<>"调入定资" AND jslb<>"转业定资" AND jslb<>"退伍定资" INTO CURSOR lst READWRITE
*!*	IF BETWEEN('201707',tnds,tnde) OR tnds>'201707'
	INSERT INTO lst SELECT TOP 1 dwbm,grbm,xm,SPACE(18) as sfzh,SPACE(7) as cjgzny,zwbm2,zjbm,dwsx,zwgw2,zwgzdc2,djc2,jbgzjb2,fddc,tgbl,jx,'物业通信' as jslb,'2017' as jsnf,'07' as jsyf,"201607" as tbnd,"201607" as jbtbz,"201501" as jxjtbz,"201707" as wytx,00000 as zwgzse2,00000 as jbgzse2,00000 as jsdjgz2,00000 as jsfszwtg2,00000 as fdgz2,00 as blfb2,jjjy2,jhljt,tgblbf,gwjt2,hj2,zgxl,tc FROM hisbase WHERE dwbm=tdwbm AND grbm=tgrbm AND jsnf+jsyf<='201707' AND jslb<>"调入定资" AND jslb<>"转业定资" AND jslb<>"退伍定资" AND jslb<>"警衔津贴" ORDER BY jsnf desc,jsyf desc,hj2 DESC
*!*	ENDIF

UPDATE lst SET wytx='201707' WHERE STRTRAN(cjgzny,".")>='201707' OR jsnf+jsyf>='201707'

SELECT * FROM lst ORDER BY jsnf,jsyf,hj2 INTO CURSOR lsz READWRITE
IF _tally>0
	&&调整变动次序
	USE IN lst
	SELECT lsz
	SCATTER MEMVAR
	SCAN
	    IF jslb='正常档次' AND jsnf='2023'
	        aaa=1
	    ENDIF
	    IF jslb="警衔变化" OR jslb="津贴变化" OR jslb="警衔津贴" OR jslb="调标晋升"
	   	    m.jslb=jslb
	   	    m.jsnf=jsnf
	   	    m.jsyf=jsyf
	   	    m.jx=jx
	   	    GATHER MEMVAR
	    ELSE
		    IF !EMPTY(m.jx)
			    REPLACE jx WITH m.jx
			ENDIF
	    ENDIF

        m.tbnd=tbnd(jsnf+jsyf,"bz06_zwgz")
        
   	    
        IF AT("警",jx)>0
            m.jxjtbz=tbnd(jsnf+jsyf,"jxjtbz")
        ENDIF
    	IF AT("法",jx)>0
        	m.jxjtbz=tbnd(jsnf+jsyf,"spjtbz")
    	ENDIF
    	IF AT("检察",jx)>0
        	m.jxjtbz=tbnd(jsnf+jsyf,"jcjtbz")
    	ENDIF


        REPLACE tbnd WITH m.tbnd,jxjtbz WITH m.jxjtbz

        &&见习人员工资
		IF AT("F",zwbm2)>0
			v_xl=ALLTRIM(SUBSTR(xl(dwbm+grbm,STRTRAN(cjgzny,".")),9))
		   	REPLACE zwbm2 WITH LEFT(zwbm2,3)+xlcc(v_xl)&&按学历重新修改zwbm2,确保zwbm2是正确的
		    m.zwbm2=zwbm2
		    
		    SELECT lsz
		    IF cyxx.jxgz=1
		        REPLACE zwgzse2 WITH jxgz06(tbnd,zwbm2,v_xl)
		    ELSE
		        REPLACE zwgzse2 WITH m.jxgz
		    ENDIF

		   	IF LEFT(zwbm2,2)>="07"
		   	    IF BITTEST(cyxx.zzrs,0)&&见习工资提高
		   	       	REPLACE jsfszwtg2 WITH zround(zwgzse2*tgbl/100)
		   	    ELSE
		   	       	REPLACE jsfszwtg2 WITH 0
		   	    ENDIF
		   	ENDIF
		   	
&&没有提取信息中的jbtbz,jbtbz在下边重新生成，所以不能在此计算dfbt2及sdbt
*!*	            REPLACE dfbt2 WITH zround(jcjx(dwbm,LEFT(zwbm2,3)+xlcc(zgxl),jbtbz)),sdbt WITH 0
    
		  	
		ELSE &&转正人员工资
			
			DO case
			CASE LEFT(zwbm2,2)="03"
				REPLACE zwgzse2 with zwgz06_fj(zwbm2,zwgzdc2,tbnd)
				REPLACE jbgzse2 WITH 0
				REPLACE jsdjgz2 WITH 0
			CASE LEFT(zwbm2,2)="21" OR LEFT(zwbm2,2)="22"
				REPLACE zwgzse2 with zwgz06(zwbm2,tbnd)
				REPLACE jbgzse2 WITH djgz06(jbgzjb2,ALLTRIM(STR(VAL(zwgzdc2)+VAL(djc2))),tbnd)
				REPLACE jsdjgz2 WITH 0
			OTHERWISE
				IF m.jxlb=2 AND (LEFT(zwbm2,2)='10' OR LEFT(zwbm2,2)='11') AND tbnd='201807'&&2018年调标，义务教育，专技
					REPLACE zwgzse2 with zwgz06_gr(zwbm2,zwgzdc2,djc2,tbnd)+zwgz06("11"+SUBSTR(zwbm2,3,2),tbnd)
				    REPLACE jbgzse2 WITH xjgz06(zwgzdc2,djc2,tbnd,"11"+SUBSTR(zwbm2,3,2))
				ELSE
					REPLACE zwgzse2 with zwgz06_gr(zwbm2,zwgzdc2,djc2,tbnd)+zwgz06(zwbm2,tbnd)
					IF INLIST(LEFT(zwbm2,2),"01","02","04",'23','24','25','26','27','28')
						REPLACE jbgzse2 WITH jbgz06(jbgzjb2,ALLTRIM(STR(VAL(zwgzdc2)+VAL(djc2))),tbnd)
					ELSE
					    IF INLIST(LEFT(zwbm2,2),"21","22")
							REPLACE jbgzse2 WITH jydjgz06(jbgzjb2,ALLTRIM(STR(VAL(zwgzdc2)+VAL(djc2))),tbnd)
						ELSE
							REPLACE jbgzse2 WITH xjgz06(zwgzdc2,djc2,tbnd,zwbm2)
						ENDIF
					ENDIF
		        ENDIF		
				REPLACE jsdjgz2 WITH jsdjgz06(zwbm2,tbnd)
			ENDCASE
			
		
			
			IF !EMPTY(fddc)
			    REPLACE fdgz2 WITH fdgz06(m.tbnd,zwbm2,zwgzdc2,fddc)
		    ENDIF
		    
		    IF LEFT(zwbm2,2)>="07" AND tgbl>0
		   	    IF tbnd='201807'
		   	        IF (m.jxlb<>2 OR LEFT(zwbm2,2)<>'10')&&非义务学校或义务学校的非专技
				     	REPLACE jsfszwtg2 WITH zround((zwgzse2+jbgzse2)*tgbl/100)
				    ELSE
				     	REPLACE jsfszwtg2 WITH zround((zwgzse2+jbgzse2)*(tgbl-10)/100)
		            ENDIF
		        ELSE
			     	REPLACE jsfszwtg2 WITH zround((zwgzse2+jbgzse2)*tgbl/100)
		        ENDIF
		    ELSE
		        REPLACE jsfszwtg2 WITH 0,tgbl WITH 0
		    ENDIF
		    
&&没有提取信息中的jbtbz,jbtbz在下边重新生成，所以不能在此计算dfbt2及sdbt
*!*		        IF INLIST(LEFT(zwbm2,2),'07','08','09','10','11') OR jslb='见习工资' OR tc='新增见习'&&后边条件tc='新增见习'用于调标晋升
*!*		            REPLACE dfbt2 WITH zround(jcjx(dwbm,zwbm2,jbtbz)),sdbt WITH 0
*!*		        ELSE
*!*	                REPLACE dfbt2 WITH zround(jcjx(dwbm,zwbm2,jbtbz)),sdbt WITH zround(sdbt(dwbm,zwbm2,jbtbz))
*!*		        ENDIF
		    
		ENDIF
		
        &&津补贴时间计算放在这里，前边有重新计算见习的zwbm2，否则查见习津补贴标准会有问题（当编码与学历没挂钩时），绩效、津补贴重新按标准计算，信息中津补贴标准时间不一定正确，在此重新修改，绩效、津补贴也在下边重新生成
        SELECT DISTINCT tbnd FROM bz06_jbt WHERE zwbm=m.zwbm2 AND tbnd<=jsnf+jsyf AND item="DFBT2" INTO ARRAY lsarray order by tbnd DESC
                
        IF _tally>0
	        m.jbtbz=lsarray[1]
            REPLACE jbtbz WITH m.jbtbz
    	ENDIF	
		
		REPLACE blfb2 WITH blfb(zwbm2)

		REPLACE zjbm WITH LEFT(zjbm(m.dwbm+m.grbm,m.dwsx,m.jsnf+m.jsyf),4)&&有的工资变动中职务级别不正确，修改任职简历不能刷新，重新获取	
		
		REPLACE hj2 WITH zwgzse2+jbgzse2+jsdjgz2+jsfszwtg2+fdgz2+blfb2+jjjy2

	    SELECT lsz
		SCATTER MEMVAR
	ENDSCAN
ENDIF

&&yzf和dfbt都放在这查找，因上边调整次序时可能调整了zwbm

&&其中 tc='新增见习'条件用于见习期调标晋升增资记录中的绩效、津补贴（津补贴不含生活性补贴）
SELECT a.*,IIF(AT("F",zwbm2)>0,zround(jcjx(dwbm,LEFT(zwbm2,3)+xlcc(zgxl),jbtbz)),IIF(INLIST(LEFT(zwbm2,2),'07','08','09','10','11') OR jslb='见习工资' OR tc='新增见习',zround(jcjx(dwbm,zwbm2,jbtbz)),zround(jcjx(dwbm,zwbm2,jbtbz))+zround(sdbt(dwbm,zwbm2,jbtbz)))) as dfbt2,0000 as jxjt,(SELECT bz FROM bz_wybt WHERE tbnd=a.wytx AND zwbm=a.zwbm2) as wybt,(SELECT bz FROM bz_txbt WHERE tbnd=a.wytx AND zwbm=a.zjbm) as txbt,(SELECT bz FROM bz_wmj WHERE jb=m.wmdw) as wmj,(SELECT pjsp FROM bz_pskhj WHERE tbnd=m.pskhjbz AND khjg='好') as pskhj FROM lsz a ORDER BY jsnf,jsyf,hj2 INTO CURSOR lszz READWRITE
IF _tally>0
    m.jf = 0
    m.zwgzse2=0
    m.jbgzse2=0
    m.jsfszwtg2=0
    m.jsdjgz2=0
    m.fdgz2=0
    m.blfb2=0
    m.jjjy2=0
    m.dfbt2=0
    m.jxjt=0
    m.gwjt2=0
    m.wybt=0
    m.txbt=0
    m.wmj=0
    
    m.yf=0
    m.bf=0
    m.qsnf=LEFT(tnds,4)
    m.qsyf=SUBSTR(tnds,5,2)

    UPDATE lszz SET jxjt = jxjt(jxjtbz,jx)+jcjt(jxjtbz,jx)+spjt(jxjtbz,jx)
	UPDATE lszz SET dfbt2=0 WHERE ISNULL(dfbt2)
	UPDATE lszz SET dfbt2=ROUND(dfbt2*10/7,0) WHERE INLIST(LEFT(zwbm2,2),"07",'08','09','10','11') AND AT("F",zwbm2)<=0
	UPDATE lszz SET jxjt=0 WHERE ISNULL(jxjt)
	UPDATE lszz SET wybt=0 WHERE ISNULL(wybt)
	UPDATE lszz SET txbt=0 WHERE ISNULL(txbt)
	UPDATE lszz SET wmj=0 WHERE ISNULL(wmj)
	UPDATE lszz SET pskhj=0 WHERE ISNULL(pskhj)
    	        	
	SELECT lszz
	GO TOP

    k=0
    SCATTER MEMVAR
	SCAN
	    IF jsnf+jsyf>tnde
	        EXIT
	    ENDIF

	    m.ys=VAL(jsnf)*12+VAL(jsyf)-VAL(m.qsnf)*12-VAL(m.qsyf)
	    IF USED("lstt") AND m.ys>0
		    k=k+1
	        INSERT INTO lstt (dwbm,grbm,xm,sfzh,jslb,ny,ys,je,startdate,enddate,zwbm2,zjbm,zwgw2,jx,jbgzjb2,zwgzdc2,djc2,zwgzse2,jbgzse2,jsfszwtg2,jsdjgz2,fdgz2,blfb2,jjjy2,dfbt2,jxjt,gwjt2,jhljt,tgblbf,wmj,wybt,txbt,pskhj,cate) VALUES (m.dwbm,m.grbm,m.xm,m.sfzh,m.jslb,m.qsnf+m.qsyf,m.ys,m.jf,tnds,IIF(subst(tnde,5,2)="01",STR(VAL(LEFT(tnde,4))-1,4,0)+"12",LEFT(tnde,4)+PADL(INT(VAL(subst(tnde,5,2))-1),2,"0")),m.zwbm2,m.zjbm,m.zwgw2,m.jx,m.jbgzjb2,m.zwgzdc2,m.djc2,m.zwgzse2,m.jbgzse2,m.jsfszwtg2,m.jsdjgz2,m.fdgz2,m.blfb2,m.jjjy2,m.dfbt2,m.jxjt,m.gwjt2,m.jhljt,m.tgblbf,m.wmj,m.wybt,m.txbt,m.pskhj,IIF(tcate=0,tcate,k))
	    ENDIF
	    IF m.ys>0
		    m.bf=m.bf+m.jf*m.ys
		ENDIF
		
	    SCATTER MEMVAR
        m.jf=zwgzse2+jbgzse2+jsfszwtg2+jsdjgz2+fdgz2+blfb2+jjjy2+dfbt2+jxjt+gwjt2+wybt+txbt&&-IIF(ISNULL(yzf),0,yzf)
        m.zwgw2=zwgw2
        m.jx=jx
        m.jbgzjb2=jbgzjb2
        m.zwgzdc2=zwgzdc2
        m.djc2=djc2     
        m.zwgzse2=zwgzse2
	    m.jbgzse2=jbgzse2
    	m.jsfszwtg2=0
	    m.jsdjgz2=jsfszwtg2
    	m.fdgz2=fdgz2
	    m.blfb2=blfb2
    	m.jjjy2=jjjy2
	    m.dfbt2=dfbt2
    	m.jxjt=jxjt
    	m.gwjt2=gwjt2
    	m.wybt=wybt
    	m.txbt=txbt
       
        IF jsnf+jsyf<tnds
		    m.qsnf=LEFT(tnds,4)
		    m.qsyf=SUBSTR(tnds,5,2)
		ELSE
	        m.qsnf=jsnf
		    m.qsyf=jsyf
		ENDIF
	    SCATTER MEMVAR
	ENDSCAN
	
	IF m.qsnf+m.qsyf<tnde
	    m.ys=(VAL(LEFT(tnde,4))*12+VAL(SUBSTR(tnde,5,2))-VAL(m.qsnf)*12-VAL(m.qsyf))
  
	    IF USED("lstt") AND m.ys>0
		    k=k+1
		    INSERT INTO lstt (dwbm,grbm,xm,sfzh,jslb,ny,ys,je,startdate,enddate,zwbm2,zjbm,zwgw2,jx,jbgzjb2,zwgzdc2,djc2,zwgzse2,jbgzse2,jsfszwtg2,jsdjgz2,fdgz2,blfb2,jjjy2,dfbt2,jxjt,gwjt2,jhljt,tgblbf,wmj,wybt,txbt,pskhj,cate) VALUES (m.dwbm,m.grbm,m.xm,m.sfzh,m.jslb,m.qsnf+m.qsyf,m.ys,m.jf,tnds,IIF(subst(tnde,5,2)="01",STR(VAL(LEFT(tnde,4))-1,4,0)+"12",LEFT(tnde,4)+PADL(INT(VAL(subst(tnde,5,2))-1),2,"0")),m.zwbm2,m.zjbm,m.zwgw2,m.jx,m.jbgzjb2,m.zwgzdc2,m.djc2,m.zwgzse2,m.jbgzse2,m.jsfszwtg2,m.jsdjgz2,m.fdgz2,m.blfb2,m.jjjy2,m.dfbt2,m.jxjt,m.gwjt2,m.jhljt,m.tgblbf,m.wmj,m.wybt,m.txbt,m.pskhj,IIF(tcate=0,tcate,k))
    	ENDIF
	    IF m.ys>0
		    m.bf=m.bf+m.jf*m.ys
    	ENDIF
	ENDIF
    USE IN lsz
    USE IN lszz    
	
    RETURN m.bf
ELSE
    USE IN lsz
    USE IN lszz    
    RETURN 000000
ENDIF
