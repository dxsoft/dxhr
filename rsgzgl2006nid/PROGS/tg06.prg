FUNCTION tg06

PARAMETERS tczwbm,tcrznx,tczwbm1,tcrznx1,tnTgnx,tcXlbm

LOCAL tcTgsj,tgjb,tgdc,jbgz,tgjb1,tgdc1,jbgz1,tgjb2,tgdc2,jbgz2
    
oldalias=ALIAS()

tcTgsj="200612"

**现任职务
IF tcZwbm>"1000" AND tcZwbm<>"10FF"&&专技
    tczwbm=LEFT(tgzwgw(tczwbm),4)
ENDIF

SELECT bz06_tgb
LOCATE FOR Zwbm=tcZwbm and BETWEEN(tcRznx,rzns,rznz) AND BETWEEN(tnTgnx,tgns,tgnz)
IF !FOUND("bz06_tgb")
    m.tgjb2="  "
    m.tgdc2="  "
    RETURN SPACE(20)+RIGHT(SPACE(4)+ALLTRIM(tcZwbm),4)+"C"
ELSE
    m.tgjb2=bz06_tgb.jb
    m.tgdc2=bz06_tgb.dc
ENDIF
m.jbgz2=jbgz06(m.tgjb2,m.tgdc2,"200607")

**前任职务
IF tcZwbm1>"1000"&&专技
    tczwbm1=LEFT(tgzwgw(tczwbm1),4)
ENDIF

SELECT bz06_tgb
LOCATE FOR Zwbm=tcZwbm1 and BETWEEN(tcRznx1,rzns,rznz) AND BETWEEN(tnTgnx,tgns,tgnz)
IF !FOUND("bz06_tgb")
    m.tgjb1="  "
    m.tgdc1="  "
ELSE
    m.tgjb1=bz06_tgb.jb
    m.tgdc1=bz06_tgb.dc
ENDIF
m.jbgz1=jbgz06(m.tgjb1,m.tgdc1,"200607")

m.tgqk=""
IF EMPTY(tczwbm1)
	m.tgjb1="  "
	m.tgdc1="  "
	m.njbgz1=0
	
	m.tgjb=tgjb2
	m.tgdc=tgdc2
ELSE
    IF LEFT(tczwbm,2)<"05"
		DO case
		CASE VAL(m.tgjb2)<VAL(m.tgjb1) AND m.jbgz2<m.jbgz1&&现级别高，但金额低于前级别,按前级别晋升到现级别
		    m.tgjb=m.tgjb2
		    m.tgdc=ALLTRIM(LEFT(jbjs06(m.tgjb1,m.tgdc1,m.tgjb,"200607"),2))
		    m.tgqk="1"

		CASE VAL(m.tgjb2)>=VAL(m.tgjb1) AND VAL(m.tgjb1)>0&&现级别低于或等于前级别,级别在前级别上晋升一级
		    m.tgjb=ALLTRIM(STR(VAL(m.tgjb1)-1))
		    IF m.jbgz2>=m.jbgz1&&现级别金额高,由现级别套入
		        m.tgdc=ALLTRIM(LEFT(jbjs06(m.tgjb2,m.tgdc2,m.tgjb,"200607"),2))
		    ELSE&&现级别金额低,由前级别套入
		        m.tgdc=ALLTRIM(LEFT(jbjs06(m.tgjb1,m.tgdc1,m.tgjb,"200607"),2))
		    ENDIF
		    m.tgqk="2"
		 OTHERWISE
	 	    m.tgjb=m.tgjb2
		    m.tgdc=m.tgdc2
	 	 ENDCASE
    ELSE
        IF LEFT(tczwbm,2)="05"&&机关工人
            IF cyxx.zwbhhjsdj='√'
                v_add=jsdjgz06(m.tczwbm,"200607")-jsdjgz06(m.tczwbm1,"200607")
			ELSE
			    v_add=0
			ENDIF

		    IF zwgz06_gr(tczwbm,m.tgdc2,"200607")+v_add<zwgz06_gr(tczwbm1,m.tgdc1,"200607")
                m.tgdc=zwjs06_gr(tczwbm1,m.tgdc1,m.tczwbm,"200607")
                m.tgjb="  "
                m.tgqk="3"
		    ELSE
		        m.tgjb="  "
		        m.tgdc=m.tgdc2
		    ENDIF

        ELSE
	        IF VAL(m.tgdc2)<VAL(m.tgdc1)
	            m.tgjb="  "
	        	m.tgdc=tgdc1
	        	m.tgqk="4"
	        ELSE
	            m.tgjb="  "
	        	m.tgdc=tgdc2
	        ENDIF
	    ENDIF
    ENDIF
ENDIF

m.xljb=""
m.xldc=""
m.xlzwbm=""
IF SUBSTR(tcZwbm,3,2)<>"FF"&& AND !INLIST(LEFT(tcZwbm,2),"05","06","08","09")&&非见习人员和工人
	m.xldz=zzdj06("200607",tcZwbm,tcXlbm)&&学历
	m.xlzwbm=LEFT(m.xldz,4)
	m.xljb=SUBSTR(m.xldz,5,2)
	m.xldc=SUBSTR(m.xldz,7,2)
	IF !EMPTY(m.xldz)
	    IF LEFT(tczwbm,2)<"05"
			IF tcZwbm>m.xlzwbm
			    tcZwbm=m.xlzwbm
			    m.tgqk="X"

				SELECT bz06_tgb
				LOCATE FOR Zwbm=tcZwbm and BETWEEN(1,rzns,rznz) AND BETWEEN(tnTgnx,tgns,tgnz)
				IF FOUND("bz06_tgb") AND bz06_tgb.jb<m.tgjb&&转正定级职务套改级别高,按学历转正职务套
			        m.tgjb=bz06_tgb.jb
			        m.tgdc=bz06_tgb.dc
				ENDIF

			ENDIF
		    DO case
			CASE VAL(m.tgjb)>VAL(m.xljb)&&学历级别高
			    IF VAL(m.tgdc)<=VAL(m.xldc) OR jbgz06(m.xljb,m.xldc,"200607")>=jbgz06(m.tgjb,m.tgdc,"200607")&&金额高
				    m.tgdc=m.xldc
				ELSE&&金额低
					m.tgdc=ALLTRIM(LEFT(jbjs06(m.tgjb,m.tgdc,m.xljb,"200607"),2))
				ENDIF
			    m.tgjb=m.xljb
	            m.tgqk="X"
			CASE VAL(m.tgjb)=VAL(m.xljb)
			    IF VAL(m.tgdc)<VAL(m.xldc)
				    m.tgdc=m.xldc
				    m.tgqk="X"
				ENDIF

			OTHERWISE&&学历级别低
			    IF jbgz06(m.xljb,m.xldc,"200607")>=jbgz06(m.tgjb,m.tgdc,"200607")
					m.tgdc=ALLTRIM(LEFT(jbjs06(m.xljb,m.xldc,m.tgjb,"200607"),2))
					m.tgqk="X"
				ENDIF

			ENDCASE
		ELSE
	        IF LEFT(tczwbm,2)="05"&&机关工人
	            IF cyxx.zwbhhjsdj='√'
	                v_add=jsdjgz06(m.tczwbm,"200607")-jsdjgz06(m.xlzwbm,"200607")
				ELSE
				    v_add=0
				ENDIF

				DO case
				CASE tcZwbm<m.xlzwbm&&学历职务低，金额高
				    IF zwgz06_gr(tczwbm,m.tgdc,"200607")+v_add<zwgz06_gr(m.xlzwbm,m.xldc,"200607")
		                m.tgdc=zwjs06_gr(m.xlzwbm,m.xldc,m.tczwbm,"200607")
		                m.tgjb="  "
		                m.tgqk="X"
				    ENDIF
				CASE tcZwbm=m.xlzwbm
				    IF VAL(m.xldc)>VAL(m.tgdc)&&职务同，档次高
				        m.tgdc=m.xldc
				        m.tgqk="X"
				    ENDIF
				    
				OTHERWISE
				    IF zwgz06_gr(tczwbm,m.tgdc,"200607")+v_add>zwgz06_gr(m.xlzwbm,m.xldc,"200607")
		                m.tgdc=zwjs06_gr(m.tczwbm,m.tgdc,m.xlzwbm,"200607")&&学历职务高金额低
		            ELSE
		                m.tgdc=m.xldc&&学历职务高，金额高
				    ENDIF
				    m.tczwbm=m.xlzwbm
	                m.tgjb="  "
	                m.tgqk="X"
	            ENDCASE

	        ELSE&&专业技术
*!*		            IF tcZwbm>m.xlzwbm
*!*		                tcZwbm=m.xlzwbm
*!*		            ENDIF
		        IF VAL(m.tgdc)<VAL(m.xldc)
		        	m.tgdc=m.xldc
		        ENDIF
	            m.tgjb="  "
		    ENDIF
	    ENDIF	
	ENDIF
ENDIF

IF EMPTY(m.tgjb) AND EMPTY(tgdc) AND SUBSTR(tcZwbm,3,2)<>"FF"&&非见习人员
    m.tgqk="C"
ENDIF

RETURN RIGHT("  "+ALLTRIM(m.tgjb),2)+RIGHT("  "+ALLTRIM(m.tgdc),2)+RIGHT("  "+ALLTRIM(m.tgjb2),2)+RIGHT("  "+ALLTRIM(m.tgdc2),2)+RIGHT("  "+ALLTRIM(m.tgjb1),2)+RIGHT("  "+ALLTRIM(m.tgdc1),2)+RIGHT("  "+ALLTRIM(m.xljb),2)+RIGHT("  "+ALLTRIM(m.xldc),2)+RIGHT(SPACE(4)+ALLTRIM(tcZwbm),4)+RIGHT(SPACE(4)+ALLTRIM(m.xlZwbm),4)+m.tgqk