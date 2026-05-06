FUNCTION jbgdtj

PARAMETERS tcdwbm,tcgrbm,tcxckhndjb,tcjsnf,tczwbm,tngl,tcjb

&&判断是否符合滚动条件,根据套改表分析，自2007年1月1日至2010年12月31期间每人只可能出现一次滚动
IF tcjsnf='2006' OR tcjsnf>"2010" OR tcxckhndjb>"2006"&&2011年前级别变动只有两条途径,1是级别滚动,2是职务变化.套改后级别滚动或晋升职务级别晋升两级及以上才修改xckhndjb。
    RETURN ""&&m.khjg
ENDIF

SEEK ALLTRIM(STR(VAL(tcjsnf)-1))+tcdwbm+tcgrbm ORDER tag ndbm IN ndkh&&上年合格
IF !FOUND('ndkh') OR !INLIST(ndkh.khjg,'优秀','合格','称职')
    m.khjg=SPACE(4)
ELSE
    m.khjg=ALLTRIM(ndkh.khjg)
ENDIF

LOCAL czwbm,nrznx,czwbm1,nrznx1,ntgnx,cxlbm,crzjl,xrzwbm,crzsj,nzwkjnx,crzsj1,nzwkjnx1,v_zw06

*!*	m.ntgnx=2006+tngl-VAL(tcjsnf)-Kjkhnx(tcdwbm+tcgrbm,'2005')   ???????????????????????
m.ntgnx=2006+tngl-VAL(tcjsnf)

m.crzjl=rzjl(tcdwbm+tcgrbm,LEFT(tczwbm,2))

IF !EMPTY(m.crzjl)
    &&套改时职务
    m.czwbm=LEFT(m.crzjl,4)
    IF SUBSTR(m.crzjl,AT(",",m.crzjl,3)+1,AT(",",m.crzjl,4)-AT(",",m.crzjl,3)-1)="1"&&部队职务
        m.czwbm=LEFT(tczwbm,2)+SUBSTR(m.czwbm,3,2)
        m.xrzwbm=xrzwbm(tcdwbm+tcgrbm)
        IF !INLIST(SUBSTR(m.czwbm,3,1),"1","2","3","4","B","C") AND LEFT(tczwbm,2)<>"07" AND m.czwbm<LEFT(ALLTRIM(m.xrzwbm)+"FFFF",4)&&部队职务高于现任职务，按虚职
            m.czwbm=LEFT(m.czwbm,3)+"1"
        ENDIF
    ENDIF
    m.crzsj=SUBSTR(m.crzjl,AT(",",m.crzjl,2)+1,AT(",",m.crzjl,3)-AT(",",m.crzjl,2)-1)
    m.nzwkjnx=VAL(SUBSTR(m.crzjl,AT(",",m.crzjl,4)+1,AT(",",m.crzjl,5)-AT(",",m.crzjl,4)-1))
    m.nrznx=IIF(BETWEEN(VAL(LEFT(m.crzsj,4)),1940,2006),2006-VAL(LEFT(m.crzsj,4))+1-m.nzwkjnx,0)

    &&低一职务
    m.czwbm1=SUBSTR(m.crzjl,AT(",",m.crzjl,5)+1,AT(",",m.crzjl,6)-AT(",",m.crzjl,5)-1)
    IF SUBSTR(m.crzjl,AT(",",m.crzjl,8)+1,AT(",",m.crzjl,9)-AT(",",m.crzjl,8)-1)="1"&&部队职务
        m.czwbm1=LEFT(tczwbm,2)+SUBSTR(m.czwbm1,3,2)
        IF !INLIST(SUBSTR(m.czwbm1,3,1),"1","2","3","4","B","C") AND LEFT(tczwbm,2)<>"07"
            m.czwbm1=LEFT(m.czwbm1,3)+"1"
        ENDIF
    ENDIF
    IF !EMPTY(m.czwbm1)
        m.crzsj1=SUBSTR(m.crzjl,AT(",",m.crzjl,7)+1,AT(",",m.crzjl,8)-AT(",",m.crzjl,7)-1)
        m.nzwkjnx1=VAL(SUBSTR(m.crzjl,AT(",",m.crzjl,9)+1,AT(",",m.crzjl,10)-AT(",",m.crzjl,9)-1))
        m.nrznx1=IIF(BETWEEN(VAL(LEFT(m.crzsj1,4)),1940,2006),2006-VAL(LEFT(m.crzsj1,4))+1-m.nzwkjnx1,0)
    ELSE
        m.crzsj1=""
        m.nzwkjnx1=0
        m.nrznx1=0
    ENDIF
    m.jzgb=SUBSTR(m.crzjl,AT(",",m.crzjl,3)+1,AT(",",m.crzjl,4)-AT(",",m.crzjl,3)-1)

    v_zw06=m.crzjl&&zw06(tcdwbm+tcgrbm,LEFT(tczwbm,2))
    IF m.czwbm<>LEFT(v_zw06,AT(",",v_zw06)-1) AND LEFT(m.czwbm,3)=LEFT(v_zw06,3)&&任职简历屏蔽掉了领导职务以保证套改年限按非领导计算，但职务按现任职务
        m.czwbm=LEFT(v_zw06,4)
    ENDIF
    
    IF !EMPTY(m.czwbm1) AND LEFT(tczwbm,2)>="10" AND m.jzgb<>"1"
        IF m.czwbm<zjbm06(tcdwgrbm) &&由较高等级的岗位聘用到较低等级的岗位
            m.v_xx=m.czwbm
            m.czwbm=m.czwbm1
            m.czwbm1=m.v_xx
            m.v_xx=m.cxrzw
            m.cxrzw=m.cxrzw1
            m.cxrzw1=m.v_xx
            m.v_xx=m.crzsj
            m.crzsj=m.crzsj1
            m.crzsj1=m.v_xx
            m.v_xx=m.nzwkjnx
            m.nzwkjnx=m.nzwkjnx1
            m.nzwkjnx1=m.v_xx
            m.v_xx=m.nrznx
            m.nrznx=m.nrznx1
            m.nrznx1=m.v_xx

            IF m.crzsj1<m.crzsj
                m.nrznx=m.nrznx1
            ENDIF
        ENDIF
    ENDIF
ELSE
    m.czwbm=""
    m.czwbm1=""
    m.nrznx=0
    m.nrznx1=0
ENDIF

m.tgjbdc=tg06(m.czwbm,m.nrznx,m.czwbm1,m.nrznx1,m.ntgnx,LEFT(xl(tcdwbm+tcgrbm,"200607"),2))&&重新套改

&&比较
*************
SELECT tgjb,gdjb,ddjb,tgzwbm,zwbm,xlbm FROM tgxx WHERE dwbm+grbm=tcdwbm+tcgrbm INTO ARRAY atgxx
IF _tally>0
    m.tgjb=atgxx[1]
    m.gdjb=atgxx[2]
    m.ddjb=atgxx[3]
    m.tgzwbm=atgxx[4]
    m.tgszwbm=atgxx[5]
    m.tgxlzwbm=LEFT(zzdj06("200607","01xx",atgxx[6]),4)
    RELEASE atgxx
ELSE
    m.tgszwbm=zw06(tcdwbm+tcgrbm,LEFT(tczwbm,2)+'FF')
    m.tgxlzwbm=SUBSTR(m.tgjbdc,21,4)
	IF m.tgszwbm>m.tgxlzwbm
		m.tgzwbm=m.tgxlzwbm
	ELSE
		m.tgzwbm=m.tgszwbm
	ENDIF

    m.tgjb=LEFT(m.tgjbdc,2)
    m.gdjb=0
    m.ddjb=0
ENDIF

IF m.tgzwbm="01FF" OR EMPTY(m.tgzwbm)
    RETURN ""
ENDIF

IF INLIST(LEFT(m.tgZwbm,2),"01","02","03")
    m.tgZwbm=LEFT(m.tgZwbm,3)+"0"
ENDIF

m.v_rzjl=rzjlall(tcdwbm+tcgrbm,LEFT(tczwbm,2))

IF EMPTY(m.v_rzjl)
*!*	    MESSAGEBOX("个人编码为 "+tcgrbm+" 的任职简历信息中缺少套改前的任职信息, 对该人员的晋升条件判断可能不正确, 请补充任职信息．"+CHR(10)+CHR(13)+CHR(10)+CHR(13)+"",64,"系统提示")
ELSE
    &&查找套改时职务
    i=0
    v_zwbm= LEFT(m.v_rzjl,4)

    IF INLIST(LEFT(v_zwbm,2),"01","02","03")
        v_zwbm=LEFT(v_zwbm,3)+"0"
    ENDIF
    IF m.tgzwbm<>v_zwbm AND SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+2)+1,7)>="2006.07"
        i=5
        DO WHILE .T.
            v_zwbm=SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i)+1,AT(",",m.v_rzjl,i+1)-AT(",",m.v_rzjl,i)-1)
		    IF INLIST(LEFT(v_zwbm,2),"01","02","03")
		        v_zwbm=LEFT(v_zwbm,3)+"0"
		    ENDIF

            IF !EMPTY(v_zwbm) AND m.tgzwbm<>v_zwbm AND SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+2)+1,7)>="2006.07"
                i=i+5
            ELSE
                EXIT
            ENDIF
        ENDDO
    ENDIF
*!*	    IF EMPTY(v_zwbm)
*!*	        MESSAGEBOX("个人编码为 "+tcgrbm+" 的同志缺少套改前的任职简历信息,对该人员的晋升条件判断可能不正确, 请核实．"+CHR(10)+CHR(13)+CHR(10)+CHR(13),64,"系统提示")
*!*	        RETURN ""
*!*	    ENDIF

    &&查找套改时职务结束
    &&2011年前级别变动只有两条途径,1是级别滚动,2是职务变化.以下是判断套改后是否级别滚动或晋升职务级别晋升两级及以上,
    &&学历高套时现职务与高套职务比较,否则与套改时职务比较,2010.09.09修改,改前为统一与套改时职务比较,对于学历高套者判断有误
    IF (m.tgzwbm<>v_zwbm AND !EMPTY(v_zwbm) AND m.tgxlzwbm==m.tgzwbm) OR tgzwbm<v_zwbm&&学历高套
        m.xlgt=.T.
	    IF VAL(m.tgjb)-VAL(tcJb)-m.gdjb+m.ddjb>IIF(UPPER(SUBSTR(m.tgzwbm,3,1))="A",10,IIF(UPPER(SUBSTR(m.tgzwbm,3,1))="B",11,IIF(UPPER(SUBSTR(m.tgzwbm,3,1))="C",12,VAL(SUBSTR(m.tgzwbm,3,1)))))-IIF(UPPER(SUBSTR(tczwbm,3,1))="A",10,IIF(UPPER(SUBSTR(tczwbm,3,1))="B",11,IIF(UPPER(SUBSTR(tczwbm,3,1))="C",12,VAL(SUBSTR(tczwbm,3,1)))))
	        RETURN ""
	    ENDIF
    ELSE
        m.xlgt=.F.
	    IF VAL(m.tgjb)-VAL(tcJb)-m.gdjb+m.ddjb>IIF(UPPER(SUBSTR(v_zwbm,3,1))="A",10,IIF(UPPER(SUBSTR(v_zwbm,3,1))="B",11,IIF(UPPER(SUBSTR(v_zwbm,3,1))="C",12,VAL(SUBSTR(v_zwbm,3,1)))))-IIF(UPPER(SUBSTR(tczwbm,3,1))="A",10,IIF(UPPER(SUBSTR(tczwbm,3,1))="B",11,IIF(UPPER(SUBSTR(tczwbm,3,1))="C",12,VAL(SUBSTR(tczwbm,3,1)))))
	        RETURN ""
	    ENDIF
    ENDIF
    &&

	oldalias=ALIAS()
    IF LEFT(tczwbm,3)<>LEFT(IIF(INLIST(LEFT(m.tgzwbm,2),"01","02","03"),LEFT(m.tgzwbm,3)+"0",m.tgzwbm),3)&&职务发生变化，取原任职务 &&OR SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+2)+1,7)>="2006.07"不能根据现任职时间是否在套改后，因为存在套改时就按较高职务套的2008.12.23修改
        &&职务发生变化，取原任职务
        &&虚职变实职不属于职务变化,取代码前3位不同才为变化
	    v_rznx = VAL(tcjsnf)-VAL(SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+2)+1,4))+1-VAL(SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+4)+1,AT(",",m.v_rzjl,i+5)-AT(",",m.v_rzjl,i+4)-1))

		SELECT bz06_tgb
		LOCATE FOR zwbm=v_zwbm AND BETWEEN(v_rznx,rzns,rznz) AND BETWEEN(tngl,tgns,tgnz)
		IF FOUND("bz06_tgb") AND VAL(bz06_tgb.jb)<VAL(m.tgjb)+IIF(m.xlgt,1,0)&&原职务达到上一级别规定年限
		     RETURN "原职"+m.khjg
		ENDIF

	    m.v_zwbm=SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+5)+1,AT(",",m.v_rzjl,i+6)-AT(",",m.v_rzjl,i+5)-1)
	    IF !EMPTY(m.v_zwbm)
	        m.v_rznx = VAL(tcjsnf)-VAL(SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+7)+1,4))+1-VAL(SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+9)+1,AT(",",m.v_rzjl,i+10)-AT(",",m.v_rzjl,i+9)-1))

		    LOCATE FOR zwbm=m.v_zwbm and BETWEEN(m.v_rznx,rzns,rznz) AND BETWEEN(tngl,tgns,tgnz)	    
			IF FOUND("bz06_tgb") AND VAL(bz06_tgb.jb)<=VAL(m.tgjb)&&原任低一职务达到套改确定级别规定年限
			     RETURN "原低"+m.khjg
			ENDIF
	    ENDIF

    ELSE
	    v_rznx = VAL(tcjsnf)-VAL(SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+2)+1,4))+1-VAL(SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+4)+1,AT(",",m.v_rzjl,i+5)-AT(",",m.v_rzjl,i+4)-1))

		SELECT bz06_tgb
		LOCATE FOR zwbm=v_zwbm AND BETWEEN(v_rznx,rzns,rznz) AND BETWEEN(tngl,tgns,tgnz)
		IF FOUND("bz06_tgb") AND VAL(bz06_tgb.jb)<VAL(m.tgjb)+IIF(m.xlgt,1,0)&&现职务达到上一级别规定年限
		     IF LEFT(tczwbm,3)<>LEFT(IIF(INLIST(LEFT(m.tgzwbm,2),"01","02","03"),LEFT(m.tgzwbm,3)+"0",m.tgzwbm),3)&&职务发生变化
    		     RETURN "原职"+m.khjg
    		 ELSE
     		     RETURN "现职"+m.khjg
     		 ENDIF
		ENDIF

	    m.v_zwbm=SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+5)+1,AT(",",m.v_rzjl,i+6)-AT(",",m.v_rzjl,i+5)-1)
	    IF !EMPTY(m.v_zwbm)
	        m.v_rznx = VAL(tcjsnf)-VAL(SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+7)+1,4))+1-VAL(SUBSTR(m.v_rzjl,AT(",",m.v_rzjl,i+9)+1,AT(",",m.v_rzjl,i+10)-AT(",",m.v_rzjl,i+9)-1))

		    LOCATE FOR zwbm=m.v_zwbm and BETWEEN(m.v_rznx,rzns,rznz) AND BETWEEN(tngl,tgns,tgnz)	    
			IF FOUND("bz06_tgb") AND VAL(bz06_tgb.jb)<=VAL(m.tgjb)&&原任低一职务达到套改级别规定年限
		     IF LEFT(tczwbm,3)<>LEFT(IIF(INLIST(LEFT(m.tgzwbm,2),"01","02","03"),LEFT(m.tgzwbm,3)+"0",m.tgzwbm),3)&&职务发生变化
			     RETURN "原低"+m.khjg
			 ELSE
			     RETURN "现低"+m.khjg
			 ENDIF
			ENDIF
	    ENDIF

	ENDIF
	
    IF !EMPTY(oldalias)
	    SELECT (oldalias)
	ENDIF
ENDIF

RETURN ""