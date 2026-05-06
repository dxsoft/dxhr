FUNCTION zwbhjs06&&正常职务变化，返回变化后的级别和档次

PARAMETERS tcyzwbm,tcyjb,tcydc,tcZwbm,tcSrny,m.xckhndzw,m.xckhndjb

tcdjc=""&&

LOCAL jslb,jsnf,jsyf

IF tcZwbm='01B1'
	tcZwbm='01B0'
ENDIF

IF SUBSTR(tcSrny,5,2)="12"
    tcTbnd=tbnd(ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1))+"01","bz06_zwgz")
ELSE
    tcTbnd=tbnd(STRTRAN(tcSrny,"."),"bz06_zwgz")
ENDIF

IF INLIST(LEFT(tcyzwbm,2),"01","02") AND LEFT(tcyzwbm,3)=LEFT(tcZwbm,3)&&行政人员只晋升职务级别，执行工资职务层次无变化(一般是军转干部)或虚职变实职
	v_jb=tcyjb
	v_xdc=tcydc
ELSE
	v_jb=""
	v_xdc=""
	DO case
	CASE LEFT(tcyzwbm,2)="01" OR LEFT(tcyzwbm,2)="04"
        IF IIF(LEFT(tcyzwbm,2)='04',SUBSTR(tcyzwbm,4,1)+"0",SUBSTR(tcyzwbm,3,2))>IIF(LEFT(tcZwbm,2)='04',SUBSTR(tcZwbm,4,1)+"0",SUBSTR(tcZwbm,3,2))&&升职务,考虑司法辅助类
*!*				IF VAL(tcyjb)>VAL(LEFT(jbscope(tczwbm),2))
            tzdjb=LEFT(jbscope(IIF(LEFT(tcZwbm,2)='04',"01"+SUBSTR(tcZwbm,4,1)+"0",tcZwbm)),2)
*!*	            tzdjb=LEFT(jbscope(tczwbm),2)
			IF VAL(tcyjb)>VAL(tzdjb)
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
			    IF VAL(tcyjb)-VAL(v_jb)>=2&&超过２级,晋升级别的考核年限从晋升职务变动级别的当年起重新计算,变动级别是从次月变动，20260115改回去（不知什么时候该的职务变化当年），进档年度同
			        m.xckhndjb=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
*!*				        m.xckhndjb=LEFT(tcSrny,4)
			    ENDIF
			    IF VAL(SUBSTR(jbjs06(tcyjb,tcydc,v_jb,tctbnd),3))=1&&超档差
			        m.xckhndzw=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
*!*				        m.xckhndzw=LEFT(tcSrny,4)
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
*!*				        m.xckhndjb=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
			        m.xckhndjb=LEFT(tcSrny,4)
			    ENDIF
			    IF VAL(SUBSTR(jbjs06(tcyjb,tcydc,v_jb,tctbnd),3))=1&&超档差
*!*				        m.xckhndzw=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
			        m.xckhndzw=LEFT(tcSrny,4)
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

	CASE LEFT(tcyzwbm,2)="03"
		IF INLIST(tczwbm,"0319","0329","031A","032A","031B","032B","031C","032C","031D","032D")&&晋升一级法官 检察官及以下职务等级，减1档，不够减时考核年度重新计算
		    IF VAL(tcydc)<=2
		        v_xdc="1"
		    ELSE
		        v_xdc=ALLTRIM(STR(VAL(tcydc)-1))&&减1档
		    ENDIF
		    IF VAL(tcydc)=1
		    	m.xckhndzw=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
		    ENDIF
		ELSE
		    IF VAL(tcydc)<=3&&晋升四级高级法官 检察官及以上职务等级，减2档，不够减时考核年度重新计算
		        v_xdc="1"
		    ELSE
		        v_xdc=ALLTRIM(STR(VAL(tcydc)-2))&&减2档
		    ENDIF
		    IF VAL(tcydc)<=2
		    	m.xckhndzw=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
		    ENDIF
		ENDIF
						
	CASE INLIST(LEFT(tcyzwbm,2),"05","06")&&机关工人
	    v_xdc=zwjs06_gr(tcyzwbm,tcydc,tcdjc,tczwbm,tctbnd)
	    IF m.cdchjsdj="√"
	        v_add=jsdjgz06(tcZwbm,tcTbnd)-jsdjgz06(tcyZwbm,tcTbnd)
	    ELSE
	        v_add=0
	    ENDIF
	    
	    IF zwgz06_gr(tcZwbm,v_xdc,'',tcTbnd)+v_add>zwgz06_gr(tcyZwbm,ALLTRIM(STR(VAL(tcydc)+1)),'',tcTbnd)&&超档差
	        IF RIGHT(tcSrny,2)>='10' and cyxx.zwbh10='√' 
	            m.xckhndzw=allt(str(val(LEFT(tcSrny,4))+1))&&下一年
	        ELSE
	            m.xckhndzw=LEFT(tcSrny,4)&&晋升年份
	        ENDIF
	    ENDIF

	CASE LEFT(tcyzwbm,2)="21" OR LEFT(tcyzwbm,2)="22"
        IF tcyzwbm>tcZwbm&&升职务
			IF VAL(tcyjb)>VAL(LEFT(jbscope(tczwbm),2))
			    v_jb=LEFT(jbscope(tczwbm),2)
			ELSE&&已达最低级别
			    IF INLIST(tcyzwbm,'2104','2106','2108','2110','2204','2206','2208','2210')&&四晋三，二晋一,级别工资不变
			        v_jb=tcyjb
			    ELSE
	    		    v_jb=RIGHT("0"+ALLTRIM(STR(VAL(tcyjb)-1)),2)
	    		ENDIF
			ENDIF

	   		IF v_jb=tcyjb
	   		    v_xdc=tcydc
	   		ELSE&&级别发生变化
		   		v_xdc=ALLTRIM(LEFT(jbjs06(tcyjb,tcydc,v_jb,tctbnd),2))
			    IF (VAL(tcyjb)-VAL(v_jb)>=2) OR INLIST(tcyzwbm,'2104','2106','2108','2110','2204','2206','2208','2210')&&超过２级 或 四晋三，二晋一进最低（20230327加此条件，不加会造成提前考核晋升还不如不晋升，先晋最低，再考核晋升）
*!*				        m.xckhndjb=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
			        m.xckhndjb=LEFT(tcSrny,4)&&从职务变化的当年重新计算
			    ENDIF
			    IF VAL(SUBSTR(jbjs06(tcyjb,tcydc,v_jb,tctbnd),3))=1&&超档差
*!*				        m.xckhndzw=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
			        m.xckhndzw=LEFT(tcSrny,4)
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

	CASE INLIST(LEFT(tcyzwbm,2),"23","24","25","26","27","28")
        IF tcyzwbm>tcZwbm&&升职务
			IF VAL(tcyjb)>VAL(LEFT(jbscope(tczwbm),2))
			    v_jb=LEFT(jbscope(tczwbm),2)
			ELSE&&已达最低级别
			    IF INLIST(tcyzwbm,'2304','2306','2308','2310','2404','2406','2408','2410','2504','2506','2508','2510','2604','2606','2608','2610','2704','2706','2708','2710','2804','2806','2808','2810') ;
			    AND INLIST(tczwbm,'2303','2305','2307','2309','2403','2405','2407','2409','2503','2505','2507','2509','2603','2605','2607','2609','2703','2705','2707','2709','2803','2805','2807','2809')&&四升三，二升一，and 后20240408加，保证是晋1级，晋2级，级别是要变动的，已达最低，级别不变
			        v_jb=tcyjb
			    ELSE
	    		    v_jb=RIGHT("0"+ALLTRIM(STR(VAL(tcyjb)-1)),2)
	    		ENDIF
			ENDIF

	   		IF v_jb=tcyjb
	   		    v_xdc=tcydc
	   		ELSE
		   		v_xdc=ALLTRIM(LEFT(jbjs06(tcyjb,tcydc,v_jb,tctbnd),2))
		   		&&超过２级 或 四晋三，二晋一进最低(and 后20240408加，保证是晋1级)（20230327加此条件，不加会造成提前考核晋升还不如不晋升，先晋最低，再考核晋升）
			    IF VAL(tcyjb)-VAL(v_jb)>=2 OR (VAL(tcyjb)-VAL(v_jb)>=1 AND ((SUBSTR(tcyzwbm,3,2)='04' AND SUBSTR(tczwbm,3,2)='03') OR (SUBSTR(tcyzwbm,3,2)='06' AND SUBSTR(tczwbm,3,2)='05') OR (SUBSTR(tcyzwbm,3,2)='08' AND SUBSTR(tczwbm,3,2)='07') OR (SUBSTR(tcyzwbm,3,2)='10' AND SUBSTR(tczwbm,3,2)='09')))
*!*				        m.xckhndjb=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
			        m.xckhndjb=LEFT(tcSrny,4)
			    ENDIF
			    IF VAL(SUBSTR(jbjs06(tcyjb,tcydc,v_jb,tctbnd),3))=1&&超档差
*!*				        m.xckhndzw=IIF(SUBSTR(tcSrny,5,2)="12",ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1)),LEFT(tcSrny,4))
			        m.xckhndzw=LEFT(tcSrny,4)
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

	CASE LEFT(tcyzwbm,2)='07' AND LEFT(tczwbm,2)='07'&&管理人员岗位等级晋升或职员等级晋升
 		IF STUFF(tcyzwbm,3,1,'')>STUFF(tcZwbm,3,1,'')&&升职务
			IF VAL(tcydc)<VAL(LEFT(jbscope(tczwbm),2))&&低于最低薪级
			    v_xdc=LEFT(jbscope(tczwbm),2)
			    m.xckhndzw=ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1))&&晋升年份，事业人员，从职务变化的下年起重算
			ELSE
			    v_xdc=tcydc
			ENDIF
		ELSE&&由较高等级调整到较低等级岗位，薪级不变
		    v_xdc=tcydc
		ENDIF

	CASE (LEFT(tcyzwbm,2)='07' AND LEFT(tczwbm,2)='10') OR (LEFT(tcyzwbm,2)='10' AND LEFT(tczwbm,2)='07') OR (LEFT(tcyzwbm,2)='08' AND LEFT(tczwbm,2)='09') OR (LEFT(tcyzwbm,2)='09' AND LEFT(tczwbm,2)='08')&&管理、专技岗位之间，工勤岗之间转换
		IF VAL(tcydc)<VAL(LEFT(jbscope(tczwbm),2))&&低于最低薪级
		    v_xdc=LEFT(jbscope(tczwbm),2)
		    m.xckhndzw=ALLTRIM(STR(VAL(LEFT(tcSrny,4))+1))&&晋升年份，事业人员，从职务变化的下年起重算
		ELSE
		    v_xdc=tcydc
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

RETURN RIGHT(SPACE(2)+ALLTRIM(v_jb),2)+RIGHT(SPACE(2)+ALLTRIM(v_xdc),2)