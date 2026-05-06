FUNCTION zwbhjs06_j
&&降职处分
PARAMETERS tcyzwbm,tcyjb,tcydc,tcZwbm,tcSrny,m.xckhndzw,m.xckhndjb,tnjljb,tnjldc

IF tcZwbm='01B1'
	tcZwbm='01B0'
ENDIF

IF TYPE("tnjljb")<>"N"
   tnjljb=0
ENDIF
IF TYPE("tnjldc")<>"N"
    tnjldc=0
ENDIF


IF SUBSTR(tcSrny,5,2)="12"
    tcTbnd=tbnd(ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1))+"01","bz06_zwgz")
ELSE
    tcTbnd=tbnd(STRTRAN(tcSrny,"."),"bz06_zwgz")
ENDIF


*!*	IF INLIST(LEFT(tcyzwbm,2),"01","02") AND LEFT(tcyzwbm,3)=LEFT(tcZwbm,3)&&行政人员只晋升职务级别，执行工资职务层次无变化(一般是军转干部)或虚职变实职，降资处分不存在
*!*		v_jb=tcyjb
*!*		v_xdc=tcydc
*!*	ELSE
	v_jb=""
	v_xdc=""
	DO case
	CASE LEFT(tcyzwbm,2)="01" OR LEFT(tcyzwbm,2)="02" OR LEFT(tcyzwbm,2)="03" OR LEFT(tcyzwbm,2)="21" OR LEFT(tcyzwbm,2)="22" OR LEFT(tcyzwbm,2)="23" OR LEFT(tcyzwbm,2)="24" OR LEFT(tcyzwbm,2)="25" OR LEFT(tcyzwbm,2)="26" OR LEFT(tcyzwbm,2)="27" OR LEFT(tcyzwbm,2)="28"
		IF tnjljb>0 OR tnjldc>0&&奖惩信息里录入有降低的级别或档次，按录入的处理，否则按文件规定处理
		    v_jb=val(tcyjb)+tnjljb
		    IF v_jb>27&&最低降至27级
		        v_jb=27
		    ENDIF
		    v_jb=ALLTRIM(STR(v_jb))

	   		v_xdc=IIF(VAL(tcydc)>tnjldc,ALLTRIM(STR(VAL(tcydc)-tnjldc)),"1")
		ELSE
			dwsx = LEFT(tcyzwbm,2)
			DO case
			CASE tcyzwbm="01A0"
			    tcyzwbm="0200"
			CASE tcyzwbm="01A1"
			    tcyzwbm="0200"
			CASE tcyzwbm="01B0"
			    tcyzwbm="0210"
			CASE tcyzwbm="01B1"
			    tcyzwbm="0210"
			CASE tcyzwbm="01C0"
			    tcyzwbm="0220"
			ENDCASE

			DO case
			CASE tczwbm="01A0"
			    tczwbm="0200"
			CASE tczwbm="01A1"
			    tczwbm="0200"
			CASE tczwbm="01B0"
			    tczwbm="0210"
			CASE tczwbm="01B1"
			    tczwbm="0210"
			CASE tczwbm="01C0"
			    tczwbm="0220"
			ENDCASE

		    IF LEFT(tcyzwbm,2)<"20"
		        IF m.dwsx="02"&&执法勤务类，级别反应在第四位
	        	    jdjb = (VAL(LEFT(tcZwbm,4))-VAL(LEFT(tcyzwbm,4)))*2&&每降低一个职务层次相应降低两个级别
	        	ELSE
	        	    jdjb = (VAL(LEFT(tcZwbm,3))-VAL(LEFT(tcyzwbm,3)))*2&&每降低一个职务层次相应降低两个级别
	        	ENDIF
	        ELSE
			    jdjb = (VAL(SUBSTR(tcZwbm,3))-VAL(SUBSTR(tcyzwbm,3)))*2&&每降低一个职务层次相应降低两个级别
	        ENDIF
	        
		    v_jb=val(tcyjb)+jdjb
		    IF v_jb>27&&最低降至27级
		        v_jb=27
		    ENDIF
		    v_jb=ALLTRIM(STR(v_jb))

	   		v_xdc=ALLTRIM(LEFT(jbjs06(tcyjb,tcydc,v_jb,tctbnd),2))
*!*		    IF VAL(tcyjb)-VAL(v_jb)>=2&&超过２级，处分不影响起始年度
*!*		        m.xckhndjb=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
*!*		    ENDIF
*!*		    IF VAL(SUBSTR(jbjs06(tcyjb,tcydc,v_jb,tctbnd),3))=1&&超档差
*!*		        m.xckhndzw=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
*!*		    ENDIF
        ENDIF

	CASE INLIST(LEFT(tcyzwbm,2),"05","06")&&机关工人
        IF tcyzwbm="0505" OR tcyzwbm="0601"&&最低岗位或普工，降2档，最低降到1档
            IF VAL(tcydc)>2
	            v_xdc=ALLTRIM(STR(VAL(tcydc)-2))
	        ELSE
	            v_xdc=1
	        ENDIF
	    ELSE
		    v_xdc=zwjs06_gr_j(tcyzwbm,tcydc,tczwbm,tctbnd)
		    IF m.cdchjsdj="√"
		        v_add=jsdjgz06(tcZwbm,tcTbnd)-jsdjgz06(tcyZwbm,tcTbnd)
		    ELSE
		        v_add=0
		    ENDIF
		ENDIF
	    
*!*		    IF zwgz06_gr(tcZwbm,v_xdc,tcTbnd)+v_add>zwgz06_gr(tcyZwbm,ALLTRIM(STR(VAL(tcydc)+1)),tcTbnd)&&超档差
*!*		        IF RIGHT(tcSrny,2)>='10' and cyxx.zwbh10='√' 
*!*		            m.xckhndzw=allt(str(val(LEFT(tcSrny,4))+1))&&下一年
*!*		        ELSE
*!*		            m.xckhndzw=LEFT(tcSrny,4)&&晋升年份
*!*		        ENDIF
*!*		    ENDIF

	OTHERWISE

	    IF tcyzwbm="1013" OR tcyzwbm="07C0" OR tcyzwbm="0805"&&无岗位等级可降的，不降低岗位工资，薪级工资按降低两级确定，最低降至薪级工资l级
		    v_xdc=ALLTRIM(STR(VAL(tcydc)-2))
			IF VAL(v_xdc)<=0
			    v_xdc="1"
			ENDIF
		ELSE
		&&薪级工资按每降低一个岗位等级相应降低两级薪级工资确定，最低降至新聘(任)岗位的起点薪级
		    v_xdc=LEFT(jbscope(tczwbm),2)&&新聘岗位起点薪级

			DO case
			CASE tcyzwbm="07A0" OR tcyzwbm="070A" OR tcyzwbm="071A"
			    tcyzwbm="0800"
			CASE tcyzwbm="07B0" OR tcyzwbm="070B" OR tcyzwbm="071B"
			    tcyzwbm="0810"
			CASE tcyzwbm="07C0" OR tcyzwbm="070C" OR tcyzwbm="071C"
			    tcyzwbm="0820"
			ENDCASE
			DO case
			CASE tczwbm="07A0" OR tczwbm="070A" OR tczwbm="071A"
			    tczwbm="0800"
			CASE tczwbm="07B0" OR tczwbm="070B" OR tczwbm="071B"
			    tczwbm="0810"
			CASE tczwbm="07C0" OR tczwbm="070C" OR tczwbm="071C"
			    tczwbm="0820"
			ENDCASE

			DO case
			CASE INLIST(tczwbm,"1002","1003","1004")
			    tczwbm="1010"
			CASE INLIST(tczwbm,"1005","1006","1007")
			    tczwbm="1020"
			CASE INLIST(tczwbm,"1008","1009","1010")
			    tczwbm="1030"
			CASE INLIST(tczwbm,"1011","1012")
			    tczwbm="1040"
			CASE INLIST(tczwbm,"1013")
			    tczwbm="1050"
			ENDCASE
			DO case
			CASE INLIST(tcyzwbm,"1002","1003","1004")
			    tcyzwbm="1010"
			CASE INLIST(tcyzwbm,"1005","1006","1007")
			    tcyzwbm="1020"
			CASE INLIST(tcyzwbm,"1008","1009","1010")
			    tcyzwbm="1030"
			CASE INLIST(tcyzwbm,"1011","1012")
			    tcyzwbm="1040"
			CASE INLIST(tcyzwbm,"1013")
			    tcyzwbm="1050"
			ENDCASE

            &&一个职务降2档
            IF INLIST(tczwbm,"0501","0502","0503","0504","0505","0801","0802","0803","0804","0805")
    		    jdjb=(VAL(tczwbm)-VAL(tcyzwbm))*m.jzdc
    		ELSE
    		    jdjb=(VAL(tczwbm)-VAL(tcyzwbm))*m.jzdc/10
            ENDIF    		
		    IF VAL(tcydc)-jdjb>VAL(v_xdc)&&高于最低薪级
		        v_xdc=ALLTRIM(STR(VAL(tcydc)-jdjb))
		    ENDIF
		ENDIF
		
*!*		    m.xckhndzw=ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1))&&晋升年份，事业人员，从职务变化的下年起重算

	ENDCASE
*!*	ENDIF

RETURN RIGHT(SPACE(2)+ALLTRIM(v_jb),2)+RIGHT(SPACE(2)+ALLTRIM(v_xdc),2)