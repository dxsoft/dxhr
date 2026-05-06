PROCEDURE ts

PARAMETERS tdwbm,tgrbm,tdwsx,tcjgzny,tcDate
&&tcdate 是晋升时间，即执行时间，对于调入就是下月
LOCAL j,k,jslb
LOCAL v_jsnf,v_jsyf,latTb,m.jrny

DIMENSION latTb[1,7]

IF PCOUNT()>0
    m.tsrq=tcDate
ELSE
    m.tsrq=m.rq
ENDIF

IF EMPTY(STRTRAN(ryjbxx.zzny,".")) OR ISNULL(ryjbxx.zzny)
	IF tdwsx<'05'
        v_xlbm="11"
    ELSE
    	v_xlbm = LEFT(xl(tdwbm+tgrbm,tcjgzny),2)
	ENDIF
	IF EMPTY(v_xlbm)
	    MESSAGEBOX("缺少参加工作前的学历，请补充完整。",64,"提示")
	    RETURN -1
	ENDIF
    m.zzsj=zzsj06(ryjbxx.gwfl,ryjbxx.cjgzny,v_xlbm)
ELSE
    m.zzsj=ryjbxx.zzny
ENDIF

this.zzsj=STRTRAN(m.zzsj,".")

this.cjgzny=STRTRAN(tcjgzny,".")

&&应该从转正后开始推算,20130924改
m.jsnf=IIF(tcjgzny<"2006.07","2006",LEFT(tcjgzny,4))
m.jsyf=IIF(tcjgzny<"2006.07","07",RIGHT(tcjgzny,2))

&&调入定资推算，应包含调入当月的职务变化（提拔调入），不应含当月的学历变动，未考虑转岗
&&降职处理包含在职务变化中
&&2016.09.07加lb排序，此月执行的变动排在当月执行(调标)的变动后边

SELECT STRTRAN(srny,".","") as jsny,"职务变化" as jslb,xrzw,zjbm,zwjb,zwbm,xzzw,3 as lb FROM ryzwbh WHERE dwbm+grbm=tdwbm+tgrbm AND srny>=m.jsnf+"."+m.jsyf AND STRTRAN(srny,".")>m.zzsj AND STRTRAN(srny,".")<STRTRAN(m.tsrq,".") AND LEFT(zwbm,2)=IIF(tdwsx>="10","10",tdwsx) AND dj(zjbm)=zwbm ;
UNION SELECT STRTRAN(srny,".","") as jsny,IIF(tdwsx>="10","等级晋升","职级晋升") as jslb,xrzw,zjbm,zwjb,zwbm,xzzw,3 as lb FROM ryzwbh WHERE dwbm+grbm=tdwbm+tgrbm AND srny>=m.jsnf+"."+m.jsyf AND STRTRAN(srny,".")>m.zzsj AND STRTRAN(srny,".")<STRTRAN(m.tsrq,".") AND LEFT(zwbm,2)=IIF(tdwsx>="10","10",tdwsx) AND dj(zjbm)<>zwbm;
UNION SELECT STRTRAN(rzsj,".","") as jsny,"职务变化" as jslb,zwmc,IIF(UPPER(LEFT(zjbm,3))<"01B0",LEFT(zjbm,3)+"1",zjbm) as zjbm,zwjb,IIF(UPPER(LEFT(zjbm,3))<"01B0",LEFT(zjbm,3)+"1",zjbm) as zwbm,zwmc(IIF(UPPER(LEFT(zjbm,3))<"01B0",LEFT(zjbm,3)+"1",zjbm)),3 as lb FROM jdzw WHERE dwbm+grbm=tdwbm+tgrbm AND STRTRAN(rzsj,".")>=m.jsnf+m.jsyf AND STRTRAN(rzsj,".")>m.zzsj AND STRTRAN(rzsj,".")<STRTRAN(m.tsrq,".") ;
UNION SELECT STRTRAN(bysj,".","") as jsny,"学历变化" as jslb,xlbm,"","","","",3 as lb FROM xl WHERE dwbm+grbm=tdwbm+tgrbm AND STRTRAN(bysj,".","")>=m.jsnf+m.jsyf AND STRTRAN(bysj,".","")<STRTRAN(m.tsrq,".","") AND xllb<>'其它' ;
UNION SELECT STR(VAL(STRTRAN(fdsj,".",""))+800,6) as jsny,"浮动固定" as jslb,xlbm,"","","","",3 as lb FROM ryjbxx WHERE dwbm+grbm=tdwbm+tgrbm AND STR(VAL(STRTRAN(fdsj,".",""))+800,6)>=m.jsnf+m.jsyf AND STR(VAL(STRTRAN(fdsj,".",""))+800,6)<STRTRAN(m.tsrq,".","") AND !EMPTY(STRTRAN(fdsj,".","")) ;
UNION SELECT STR(VAL(STRTRAN(fdsj,".",""))+1600,6) as jsny,"浮动固定" as jslb,xlbm,"","","","",3 as lb FROM ryjbxx WHERE dwbm+grbm=tdwbm+tgrbm AND STR(VAL(STRTRAN(fdsj,".",""))+1600,6)>=m.jsnf+m.jsyf AND STR(VAL(STRTRAN(fdsj,".",""))+1600,6)<STRTRAN(m.tsrq,".","") AND !EMPTY(STRTRAN(fdsj,".","")) ;
UNION SELECT STR(VAL(STRTRAN(fdsj,".",""))+2400,6) as jsny,"浮动固定" as jslb,xlbm,"","","","",3 as lb FROM ryjbxx WHERE dwbm+grbm=tdwbm+tgrbm AND STR(VAL(STRTRAN(fdsj,".",""))+2400,6)>=m.jsnf+m.jsyf AND STR(VAL(STRTRAN(fdsj,".",""))+2400,6)<STRTRAN(m.tsrq,".","") AND !EMPTY(STRTRAN(fdsj,".","")) ;
UNION SELECT STR(VAL(STRTRAN(fdsj,".",""))+3200,6) as jsny,"浮动固定" as jslb,xlbm,"","","","",3 as lb FROM ryjbxx WHERE dwbm+grbm=tdwbm+tgrbm AND STR(VAL(STRTRAN(fdsj,".",""))+3200,6)>=m.jsnf+m.jsyf AND STR(VAL(STRTRAN(fdsj,".",""))+3200,6)<STRTRAN(m.tsrq,".","") AND !EMPTY(STRTRAN(fdsj,".","")) ;
UNION SELECT STR(VAL(STRTRAN(fdsj,".",""))+4000,6) as jsny,"浮动固定" as jslb,xlbm,"","","","",3 as lb FROM ryjbxx WHERE dwbm+grbm=tdwbm+tgrbm AND STR(VAL(STRTRAN(fdsj,".",""))+4000,6)>=m.jsnf+m.jsyf AND STR(VAL(STRTRAN(fdsj,".",""))+4000,6)<STRTRAN(m.tsrq,".","") AND !EMPTY(STRTRAN(fdsj,".","")) ;
UNION SELECT this.zzsj as jsny,"转正定级" as jslb,"","","","","",1 as lb FROM dwbm WHERE dwbm=tdwbm ;
UNION SELECT STRTRAN(hjsj,".","") as jsny,"降级处分" as jslb,"","","","","",0 as lb FROM hjxx WHERE dwbm+grbm=tdwbm+tgrbm AND STRTRAN(hjsj,".","")>=m.jsnf+m.jsyf AND STRTRAN(hjsj,".","")<STRTRAN(m.tsrq,".","") AND jllx="降级处分" ;
	ORDER BY jsny,lb INTO ARRAY latTb
    
j=_tally
k=1
DO WHILE m.jsnf+m.jsyf<=STRTRAN(m.tsrq,".","")
    IF m.jsnf+m.jsyf<STRTRAN(m.tsrq,".","") AND m.jsnf+m.jsyf>m.zzsj&&去掉调入当月的级别和档次晋升（12月调入）
	    IF AT("F",m.zwbm2)>0
	    ELSE
		    &&级别
		    IF tdwsx<"05"
			    &&级别，级别滚动条件中的套改年限需去掉考核不合格年限Kjkhnx(ryjbxx.dwbm+ryjbxx.grbm,ALLTRIM(STR(VAL(This.jsnf)-1)))，2016.9.5修改，对以前的影响：滚动前有不合格没考虑

		        v_jbjstj=jbjstj(tdwbm,tgrbm,m.xckhndjb,m.jsnf)
		        IF EMPTY(v_jbjstj)
		            v_jbjstj=jbgdtj(tdwbm,tgrbm,m.xckhndjb,m.jsnf,m.zwbm2,ROUND(VAL(m.jsnf)-VAL(LEFT(tcjgzny,4))+1+m.bjglxlnx-ryjbxx.zdgznx-Kjkhnx(ryjbxx.dwbm+ryjbxx.grbm,ALLTRIM(STR(VAL(This.jsnf)-1))),0),ryjbxx.jbgzjb2)
		        ENDIF

			    IF INLIST(RIGHT(v_jbjstj,4),"优秀","称职","合格") OR v_jbjstj=="五年称职"
					IF v_jbjstj="五年称职"
			     		m.jslb="正常级别"
			     	ELSE
			     		m.jslb="级别滚动"
			     	ENDIF

					v_zgjb=SUBSTR(jbscope(m.zwbm2),3,2) 

					IF VAL(m.jbgzjb2)>VAL(v_zgjb)&&未达最高
					    m.xjb=ALLTRIM(STR(VAL(m.jbgzjb2)-1))
			    		v_tbnd=tbnd(this.jsnf+"01","bz06_zwgz")
			    		m.xdc=ALLTRIM(LEFT(jbjs06(m.jbgzjb2,m.zwgzdc2,m.xjb,v_tbnd),2))
					    IF jbgz06(m.xjb,m.xdc,v_tbnd)>jbgz06(m.jbgzjb2,ALLTRIM(STR(VAL(m.zwgzdc2)+1)),v_tbnd)&&超档差,档次晋升年限重新算
					        m.xckhndzw = m.jsnf
					    ENDIF
					    m.zwgzdc2 = m.xdc
					    m.jbgzjb2 = m.xjb
					ELSE&&已达最高晋升一档
					    m.zwgzdc2 = ALLTRIM(STR(VAL(m.zwgzdc2)+1))
					ENDIF

			        m.xckhndjb = m.jsnf
			    ENDIF
			ENDIF
			&&级别

		    &&档次
	        SELECT DISTINCT khnd FROM ndkh WHERE INLIST(ndkh.khjg,"优秀","称职","合格","基本称职","基本合格") AND ndkh.dwbm+ndkh.grbm=tdwbm+tgrbm AND BETWEEN(khnd,m.xckhndzw,ALLTRIM(STR(VAL(m.jsnf)-1))) ORDER BY khnd INTO ARRAY lat
		    IF _tally>0
		        IF (tdwsx<"07" AND _tally>=2) OR (tdwsx>="07" AND _tally>=1)
			        v_zwgzdc=ALLTRIM(STR(VAL(m.zwgzdc2)+1))
				    &&倒档处理
				    IF VAL(v_zwgzdc)<=VAL(zgdc(m.jbgzjb2))
				        m.zwgzdc2 = v_zwgzdc
				        m.djc2 = ""
				    ELSE
				        m. zwgzdc2 = zgdc(m.jbgzjb2)
				        m.djc2 = ALLTRIM(STR(VAL(v_zwgzdc)-VAL(zgdc(m.jbgzjb2))))
				    ENDIF

				    m.xckhndzw = m.jsnf
		        ENDIF
		    ENDIF
		ENDIF
    ENDIF&&级别档次结束

    DO WHILE k<=j
        IF LEFT(latTb[k,1],4)<=m.jsnf
            DO case
            CASE latTb[k,2]="职务变化"
                this.zwbh(latTb[k,3],latTb[k,4],latTb[k,5],latTb[k,6],latTb[k,7],ALLTRIM(latTb[k,1]))
            CASE latTb[k,2]="职级晋升" AND (LEFT(latTb[k,6],2)="03" OR LEFT(latTb[k,6],2)="04")
                this.fjtg(latTb[k,3],latTb[k,4],latTb[k,5],latTb[k,6],latTb[k,7],ALLTRIM(latTb[k,1]))
            CASE latTb[k,2]="职级晋升"
                this.zjjs(latTb[k,3],latTb[k,4],latTb[k,5],latTb[k,6],latTb[k,7],ALLTRIM(latTb[k,1]))
            CASE latTb[k,2]="等级晋升"
                this.djjs(latTb[k,3],latTb[k,4],latTb[k,5],latTb[k,6],latTb[k,7],ALLTRIM(latTb[k,1]))
            CASE latTb[k,2]="学历变化"
                m.xldz=this.xljs(m.zwbm2,latTb[k,3],m.jbgzjb2,m.zwgzdc2,ALLTRIM(latTb[k,1]))
                IF !EMPTY(m.xldz)
                    IF LEFT(m.xldz,4)<m.zwbm2 OR VAL(SUBSTR(m.xldz,5,2))<>VAL(m.jbgzjb2) OR VAL(SUBSTR(m.xldz,7,2))<>VAL(m.zwgzdc2)
						IF SUBSTR(STRTRAN(latTb[k,1],".",""),5,2)="12"
						    v_jsyf="01"
						    v_jsnf=ALLTRIM(STR(VAL(LEFT(latTb[k,1],4))+1))
						ELSE
						    v_jsnf=LEFT(latTb[k,1],4)
						    v_jsyf=right('0'+ALLTRIM(STR(VAL(SUBSTR(STRTRAN(latTb[k,1],".",""),5,2))+1)),2)
						ENDIF

						v_tbnd=tbnd(v_jsnf+v_jsyf,"bz06_zwgz")

						IF INLIST(tdwsx,"01","02","03")
						    IF VAL(SUBSTR(m.xldz,5,2))<VAL(m.jbgzjb2)-1 OR (VAL(SUBSTR(m.xldz,5,2))=VAL(m.jbgzjb2)-1 AND m.zwbm2=LEFT(m.xldz,4))
						    &&2009.11.26改
						    &&2级别以上，考核年度重新计算;晋升一个级别且职务未发生变化，级别考核年度重新计算
			    			    m.xcKHNDJB = v_jsnf
			    			ENDIF
						    IF jbgz06(SUBSTR(m.xldz,5,2),SUBSTR(m.xldz,7,2),v_tbnd)>jbgz06(m.jbgzjb2,ALLTRIM(STR(VAL(m.zwgzdc2)+1)),v_tbnd)&&级别变动，超档差，档次考核年度重新计算
						        m.xcKHNDZW = v_jsnf
			        		ENDIF
						ELSE
			    			m.xcKHNDJB = ""
			    			IF v_jsyf="01"
    						    m.xcKHNDZW = v_jsnf
    						ELSE
    						    m.xcKHNDZW = ALLTRIM(STR(VAL(v_jsnf)+1))
                            ENDIF    						
						ENDIF

						m.zwbm2 = LEFT(m.xldz,4)

						m.jbgzjb2 = SUBSTR(m.xldz,5,2)
						m.zwgzdc2 = ALLTRIM(SUBSTR(m.xldz,7,2))
                    ENDIF
                ENDIF    
            CASE latTb[k,2]="浮动固定"
				v_zwgzdc=ALLTRIM(STR(VAL(ryjbxx.zwgzdc2)+VAL(ryjbxx.fddc)))

                v_jsnf=LEFT(latTb[k,1],4)
				v_jsyf=PADL(ALLTRIM(SUBSTR(latTb[k,1],5,2)),2,'0')

				m.zwgzdc2 = v_zwgzdc
				m.fdgd = m.fdgd+VAL(m.fddc)

            CASE latTb[k,2]="降级处分"

				IF SUBSTR(STRTRAN(latTb[k,1],".",""),5,2)="12"
				    v_jsyf="01"
				    v_jsnf=ALLTRIM(STR(VAL(LEFT(latTb[k,1],4))+1))
				ELSE
				    v_jsnf=LEFT(latTb[k,1],4)
				    v_jsyf=right('0'+ALLTRIM(STR(VAL(SUBSTR(STRTRAN(latTb[k,1],".",""),5,2))+1)),2)
				ENDIF
				v_tbnd=tbnd(v_jsnf+v_jsyf,"bz06_zwgz")

			    v_xjb=m.jbgzjb2
		        v_xdc=m.zwgzdc2
				DO case
				CASE INLIST(tdwsx,"01","02","03")
				    IF m.jbgzjb2=LEFT(jbscope(m.zwbm2),2)&&已是最低级别，降一个档次
	                    v_xjb=m.jbgzjb2
				        v_xdc=ALLTRIM(STR(VAL(m.zwgzdc2)-1))
				    ENDIF
				CASE INLIST(m.zwbm2,"0505","0601")&&无岗位可降，降2档，最低降至1档
				    v_xjb=m.jbgzjb2
				    v_xdc=VAL(m.zwgzdc2)-2
				    IF v_xdc>0&&最低降至1级
    			        v_xdc=ALLTRIM(STR(v_xdc))
    			    ELSE
    			        v_xdc=1
    			    ENDIF
				CASE INLIST(m.zwbm2,"0805","0601")&&事业最低等级
				    v_xjb=m.jbgzjb2
				    v_xdc=VAL(m.zwgzdc2)-2
				    IF v_xdc>0&&最低降至1级
    			        v_xdc=ALLTRIM(STR(v_xdc))
    			    ELSE
    			        v_xdc=1
    			    ENDIF
				OTHERWISE&&事业,事业只有降低岗位等级，把这部分放在职务变化里了，此处取消
				ENDCASE

				m.jbgzjb2 = v_xjb
				m.zwgzdc2 = v_xdc

            CASE latTb[k,2]="转正定级"
                this.zz(latTb[k,1])

            ENDCASE
	    ELSE
	        EXIT
	    ENDIF
        k=k+1
    ENDDO
    m.jsnf=ALLTRIM(STR(VAL(m.jsnf)+1))
    m.jsyf="01"    
ENDDO
