FUNCTION zwbhjs16 &&正常职务变化,返回级别+档次+倒级差

PARAMETERS tcyzwbm,tcyjb,tcydc,tcZwbm,tcSrny,m.xckhndzw,m.xckhndjb

LOCAL m.zgdc,v_jb,v_xdc,v_djc

v_djc=''

IF tcZwbm='01B1'
    tcZwbm='01B0'
ENDIF

*!*	IF ryjbxx.zwbm2=tczwbm&& OR (LEFT(tcZwbm,2)<>LEFT(ryjbxx.zwbm2,2))
*!*	*!*	    REPLACE xrzw WITH tcXrzw,srny WITH LEFT(tcsrny,4)+"."+RIGHT(ALLTRIM(tcsrny),2) IN ryjbxx
*!*	    RETURN
*!*	ENDIF

IF SUBSTR(tcSrny,5,2)="12"
    tcTbnd=tbnd(ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1))+"01","bz06_zwgz")
ELSE
    tcTbnd=tbnd(STRTRAN(tcSrny,"."),"bz06_zwgz")
ENDIF

IF INLIST(LEFT(tcyzwbm,2),"01","02","03") AND LEFT(tcyzwbm,3)=LEFT(tcZwbm,3)&&行政人员只晋升职务级别，执行工资职务层次无变化(一般是军转干部)或虚职变实职
	v_jb=tcyjb
	v_xdc=tcydc
ELSE
	v_jb=""
	v_xdc=""
	DO case
	CASE LEFT(tcyzwbm,2)="01"
        IF tcyzwbm>tcZwbm&&升职务
			IF VAL(tcyjb)>VAL(LEFT(jbscope(tczwbm),2))
			    v_jb=LEFT(jbscope(tczwbm),2)
			ELSE&&已达最低级别
			    IF tcyzwbm="0205" AND tczwbm="0204"&&一级警员升为四级警长,级别工资不变
			        v_jb=tcyjb
			    ELSE
	    		    v_jb=RIGHT("0"+ALLTRIM(STR(VAL(tcyjb)-1)),2)
	    		ENDIF
			ENDIF

	   		IF v_jb=tcyjb
	   		    v_xdc=tcydc
	   		ELSE
		   		v_xdc=ALLTRIM(LEFT(jbjs06(tcyjb,tcydc,v_jb,tctbnd),2))
			    IF VAL(tcyjb)-VAL(v_jb)>=2&&超过２级
			        m.xckhndjb=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
			    ENDIF
			    IF VAL(SUBSTR(jbjs06(tcyjb,tcydc,v_jb,tctbnd),3))=1&&超档差
			        m.xckhndzw=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
			    ENDIF
			ENDIF
		ELSE&&降职务
			SELECT * FROM hjxx WHERE dwbm=ryjbxx.dwbm AND grbm=ryjbxx.grbm AND (jllx='降级处分' OR jllx='行政撤职') AND STRTRAN(hjsj,".")=tcsrny INTO ARRAY lhj
			IF _tally<=0
			    RETURN ""
			ENDIF

			IF VAL(tcyjb)<VAL(SUBSTR(jbscope(tczwbm),3))&&高于最高级别，进入最高
			    v_jb=SUBSTR(jbscope(tczwbm),3)
			    v_xdc=tcydc
			ELSE&&
		        v_jb=tcyjb
			    v_xdc=tcydc
			ENDIF
		
		ENDIF

	CASE LEFT(tcyzwbm,2)="02"
        IF tcyzwbm>tcZwbm&&升职务
			IF VAL(tcyjb)>VAL(LEFT(jbscope(tczwbm),2))
			    v_jb=LEFT(jbscope(tczwbm),2)
			ELSE&&已达最低级别
			    IF tcyzwbm="0205" AND tczwbm="0204"&&一级警员升为四级警长,级别工资不变
			        v_jb=tcyjb
			    ELSE
	    		    v_jb=RIGHT("0"+ALLTRIM(STR(VAL(tcyjb)-1)),2)
	    		ENDIF
			ENDIF

	   		IF v_jb=tcyjb
	   		    v_xdc=tcydc
	   		ELSE
		   		v_xdc=ALLTRIM(LEFT(jbjs06(tcyjb,tcydc,v_jb,tctbnd),2))
			    IF VAL(tcyjb)-VAL(v_jb)>=2&&超过２级
			        m.xckhndjb=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
			    ENDIF
			    IF VAL(SUBSTR(jbjs06(tcyjb,tcydc,v_jb,tctbnd),3))=1&&超档差
			        m.xckhndzw=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
			    ENDIF
			ENDIF
		ELSE&&降职务
			IF VAL(tcyjb)<VAL(SUBSTR(jbscope(tczwbm),3))&&高于最高级别，进入最高
			    v_jb=SUBSTR(jbscope(tczwbm),3)
			    v_xdc=tcydc
			ELSE&&
		        v_jb=tcyjb
			    v_xdc=tcydc
			ENDIF
		
		ENDIF
				
	CASE INLIST(LEFT(tcyzwbm,2),"05","06")&&机关工人
	    v_xdc=zwjs06_gr(tcyzwbm,tcydc,tczwbm,tctbnd)
	    IF m.cdchjsdj="√"
	        v_add=jsdjgz06(tcZwbm,tcTbnd)-jsdjgz06(tcyZwbm,tcTbnd)
	    ELSE
	        v_add=0
	    ENDIF
	    
	    IF zwgz06_gr(tcZwbm,v_xdc,tcTbnd)+v_add>zwgz06_gr(tcyZwbm,ALLTRIM(STR(VAL(tcydc)+1)),tcTbnd)&&超档差
	        IF RIGHT(tcSrny,2)>='10' and cyxx.zwbh10='√' 
	            m.xckhndzw=allt(str(val(LEFT(tcSrny,4))+1))&&下一年
	        ELSE
	            m.xckhndzw=LEFT(tcSrny,4)&&晋升年份
	        ENDIF
	    ENDIF

	OTHERWISE
 		IF tcyzwbm>tcZwbm&&升职务
			IF VAL(tcydc)<VAL(LEFT(jbscope(tczwbm),2))&&低于最低薪级
			    v_xdc=LEFT(jbscope(tczwbm),2)
			    m.xckhndzw=ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1))&&晋升年份，事业人员，从职务变化的下年起重算
			ELSE
			    v_xdc=tcydc
			ENDIF
		ELSE&&由较高等级调整到较低等级岗位，薪级不变
		    v_xdc=tcydc
		ENDIF
	ENDCASE
ENDIF

&&计算倒档次
m.zgdc=zgdc(v_jb)
IF VAL(v_xdc)>VAL(m.zgdc)
    v_djc=ALLTRIM(STR(VAL(v_xdc)-VAL(m.zgdc)))
    v_xdc=m.zgdc
ENDIF
				
RETURN PADL(ALLTRIM(v_jb),2,' ')+PADL(ALLTRIM(v_xdc),2,' ')+PADL(ALLTRIM(v_djc),2,' ')