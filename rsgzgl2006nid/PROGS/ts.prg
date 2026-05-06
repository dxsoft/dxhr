FUNCTION ts

PARAMETERS tsjsnf,tsjsyf,tejsnf,tejsyf,m.xckhndzw,m.xckhndjb,latTb

EXTERNAL ARRAY latTb
PARAMETERS tcDate,tcDwsx



&&按着一个序列进行推算

&&
LOCAL j,k,jslb
LOCAL v_jsnf,v_jsyf,latTb
DIMENSION latTb[1,7]

IF PCOUNT()>0
    this.tsrq=tcDate
ELSE
    this.tsrq=m.rq
ENDIF

k=1
DO WHILE this.jsnf+this.jsyf<=STRTRAN(this.tsrq,".","")
    IF this.jsnf+this.jsyf<STRTRAN(this.tsrq,".","")&&去掉调入当月的级别和档次晋升（12月调入）
	    &&级别
	    v_jbjstj=this.jbjstj(tryjbxx.dwbm,tryjbxx.grbm,tryjbxx.xckhndjb,this.jsnf,tryjbxx.dwsx,tryjbxx.zwbm2,ROUND(VAL(this.jsnf)-VAL(LEFT(tryjbxx.cjgzny,4))+1-tryjbxx.zdgznx,0)+tryjbxx.bjglxlnx,tryjbxx.jbgzjb2)
	    IF INLIST(RIGHT(v_jbjstj,4),"优秀","称职","合格") OR v_jbjstj=="五年称职"
			v_zgjb=SUBSTR(jbscope(tryjbxx.zwbm2),3,2) 

			SELECT tryjbxx
	        REPLACE tryjbxx.gznx WITH VAL(this.jsnf) - VAL(LEFT(tryjbxx.cjgzny,4)) + 1 - tryjbxx.zdgznx IN tryjbxx

			IF VAL(tryjbxx.jbgzjb2)>VAL(v_zgjb)&&未达最高
			    m.xjb=ALLTRIM(STR(VAL(tryjbxx.jbgzjb2)-1))
	    		v_tbnd=tbnd(this.jsnf+"01","bz06_zwgz")
	    		m.xdc=ALLTRIM(LEFT(jbjs06(tryjbxx.jbgzjb2,tryjbxx.zwgzdc2,m.xjb,v_tbnd),2))
			    IF jbgz06(m.xjb,m.xdc,v_tbnd)>jbgz06(tryjbxx.jbgzjb2,ALLTRIM(STR(VAL(tryjbxx.zwgzdc2)+1)),v_tbnd)&&超档差,档次晋升年限重新算
			        REPLACE tryjbxx.xckhndzw WITH this.jsnf IN tryjbxx
			    ENDIF
			    REPLACE tryjbxx.zwgzdc2 WITH m.xdc IN tryjbxx
			    REPLACE tryjbxx.jbgzjb2 WITH m.xjb IN tryjbxx
			ELSE&&已达最高晋升一档
			    REPLACE tryjbxx.zwgzdc2 WITH ALLTRIM(STR(VAL(tryjbxx.zwgzdc2)+1)) IN tryjbxx
			ENDIF

	    ENDIF

	    &&档次
        SELECT DISTINCT khnd FROM ndkh WHERE INLIST(ndkh.khjg,"优秀","称职","合格","基本称职","基本合格") AND ndkh.dwbm+ndkh.grbm=tryjbxx.dwbm+tryjbxx.grbm AND BETWEEN(khnd,tryjbxx.xckhndzw,ALLTRIM(STR(VAL(this.jsnf)-1))) ORDER BY khnd INTO ARRAY lat
	    IF _tally>0
	        IF (tryjbxx.dwsx<"07" AND _tally>=2) OR (tryjbxx.dwsx>="07" AND _tally>=1)
		        v_zwgzdc=ALLTRIM(STR(VAL(tryjbxx.zwgzdc2)+1))
			    &&倒档处理
			    IF VAL(v_zwgzdc)<=VAL(zgdc(tryjbxx.jbgzjb2))
			        REPLACE zwgzdc2 WITH v_zwgzdc IN tryjbxx
			        REPLACE djc2 WITH "" IN tryjbxx
			    ELSE
			        REPLACE zwgzdc2 WITH zgdc(tryjbxx.jbgzjb2) IN tryjbxx
			        REPLACE djc2 WITH ALLTRIM(STR(VAL(v_zwgzdc)-VAL(zgdc(tryjbxx.jbgzjb2)))) IN tryjbxx
			    ENDIF

			    REPLACE xckhndzw WITH this.jsnf IN tryjbxx
	        ENDIF
	    ENDIF
	ENDIF

    DO WHILE k<=j
        IF LEFT(latTb[k,1],4)<=this.jsnf
            DO case
            CASE latTb[k,2]="职务变化"
				m.xckhndzw=tryjbxx.xckhndzw
				m.xckhndjb=tryjbxx.xckhndjb
				m.xjb_dc=zwbhjs06(tryjbxx.zwbm2,tryjbxx.jbgzjb2,tryjbxx.zwgzdc2,tczwbm,tcsrny,@m.xckhndzw,@m.xckhndjb)

				REPLACE zwbm2 WITH tczwbm IN tryjbxx
				REPLACE zwgw2 WITH tczwmc IN tryjbxx
				REPLACE xckhndzw WITH m.xckhndzw IN tryjbxx
				IF LEFT(tczwbm,2)<"05"
				    REPLACE xckhndjb WITH m.xckhndjb IN tryjbxx
				ELSE
				    REPLACE xckhndjb WITH "    " IN tryjbxx
				ENDIF
				REPLACE jbgzjb2 WITH LEFT(m.xjb_dc,2) IN tryjbxx

				v_zwgzdc=ALLTRIM(SUBSTR(m.xjb_dc,3,2))&&倒档处理
				IF VAL(v_zwgzdc)<=VAL(zgdc(tryjbxx.jbgzjb2))
				    REPLACE zwgzdc2 WITH v_zwgzdc IN tryjbxx
				    REPLACE djc2 WITH "" IN tryjbxx
				ELSE
				    REPLACE zwgzdc2 WITH zgdc(tryjbxx.jbgzjb2) IN tryjbxx
				    REPLACE djc2 WITH ALLTRIM(STR(VAL(v_zwgzdc)-VAL(zgdc(tryjbxx.jbgzjb2)))) IN tryjbxx
				ENDIF

				REPLACE xrzw WITH tcxrzw,srny WITH LEFT(tcsrny,4)+"."+SUBSTR(tcsrny,5,2) ,zwjb WITH tczwjb,zjbm WITH tczjbm IN tryjbxx

            CASE latTb[k,2]="学历变化"
                m.xldz=this.xljs(tryjbxx.zwbm2,latTb[k,3],tryjbxx.jbgzjb2,tryjbxx.zwgzdc2,latTb[k,1])
                IF !EMPTY(m.xldz)
                    IF LEFT(m.xldz,4)<tryjbxx.zwbm2 OR VAL(SUBSTR(m.xldz,5,2))<>VAL(tryjbxx.jbgzjb2) OR VAL(SUBSTR(m.xldz,7,2))<>VAL(tryjbxx.zwgzdc2)
						IF SUBSTR(STRTRAN(latTb[k,1],".",""),5,2)="12"
						    v_jsyf="01"
						    v_jsnf=ALLTRIM(STR(VAL(LEFT(latTb[k,1],4))+1))
						ELSE
						    v_jsnf=LEFT(latTb[k,1],4)
						    v_jsyf=right('0'+ALLTRIM(STR(VAL(SUBSTR(STRTRAN(latTb[k,1],".",""),5,2))+1)),2)
						ENDIF

						v_tbnd=tbnd(v_jsnf+v_jsyf,"bz06_zwgz")

						IF INLIST(tryjbxx.dwsx,"01","02","03")
						    IF VAL(SUBSTR(m.xldz,5,2))<VAL(tryjbxx.jbgzjb2)-1 OR (VAL(SUBSTR(m.xldz,5,2))=VAL(tryjbxx.jbgzjb2)-1 AND tryjbxx.zwbm2=LEFT(m.xldz,4))
						    &&2009.11.26改
						    &&2级别以上，考核年度重新计算;晋升一个级别且职务未发生变化，级别考核年度重新计算
			    			    REPLACE xcKHNDJB WITH v_jsnf IN tryjbxx
			    			ENDIF
						    IF jbgz06(SUBSTR(m.xldz,5,2),SUBSTR(m.xldz,7,2),v_tbnd)>jbgz06(tryjbxx.jbgzjb2,ALLTRIM(STR(VAL(tryjbxx.zwgzdc2)+1)),v_tbnd)&&级别变动，超档差，档次考核年度重新计算
						        REPLACE xcKHNDZW WITH v_jsnf IN tryjbxx
			        		ENDIF
						ELSE
			    			REPLACE xcKHNDJB WITH "" IN tryjbxx
			    			IF v_jsyf="01"
    						    REPLACE xcKHNDZW WITH v_jsnf IN tryjbxx
    						ELSE
    						    REPLACE xcKHNDZW WITH ALLTRIM(STR(VAL(v_jsnf)+1)) IN tryjbxx
                            ENDIF    						
						ENDIF

						REPLACE zwbm2 WITH LEFT(m.xldz,4) IN tryjbxx
						REPLACE zwgw2 WITH zwmc(LEFT(m.xldz,4)) IN tryjbxx

						REPLACE jbgzjb2 WITH SUBSTR(m.xldz,5,2) IN tryjbxx
						REPLACE zwgzdc2 WITH SUBSTR(m.xldz,7,2) IN tryjbxx
                    ENDIF
                ENDIF    
            ENDCASE
	    ELSE
	        EXIT
	    ENDIF
        k=k+1
    ENDDO
    this.jsnf=ALLTRIM(STR(VAL(this.jsnf)+1))
    this.jsyf="01"    
ENDDO

jbdc=ryjbxx.jb+ryjbxx.dc

USE IN tryjbxx
RETURN  jbdc