FUNCTION xljs

PARAMETERS tczwbm,tcxlbm,tcjb,tcdc,tcbysj

v_tbnd=tbnd(STRTRAN(tcbysj ,"."),"bz06_zwgz" )

IF m.zzgddchgr
    SELECT zzzwbm,zzjb,RIGHT("  "+ALLTRIM(STR(VAL(zzdc)+IIF(tcxlbm<"60",m.zzgddc,0))),2) FROM bz06_zzdz WHERE xlbm=tcxlbm AND LEFT(zzzwbm,2)=LEFT(tczwbm,2) INTO ARRAY lat
ELSE
    SELECT zzzwbm,zzjb,RIGHT("  "+ALLTRIM(STR(VAL(zzdc)+IIF(tcxlbm<"60" AND !INLIST(LEFT(tczwbm,2),"05","06","08","09"),m.zzgddc,0))),2) FROM bz06_zzdz WHERE xlbm=tcxlbm AND LEFT(zzzwbm,2)=LEFT(tczwbm,2) INTO ARRAY lat
ENDIF

IF _tally<=0
    RETURN ""
ENDIF

DO CASE
CASE INLIST(LEFT(tczwbm,2),"01","02","03","04","21","22","23","24","25","26","27","28")

	IF lat[1,1]<=tczwbm&&职务发生变化
    	m.zwbm=lat[1,1]

*!*			m.xckhndzw=ryjbxx.xckhndzw
*!*			m.xckhndjb=ryjbxx.xckhndjb
*!*			m.xjb_dc=zwbhjs06(tczwbm,tcjb,tcdc,m.zwbm,tcbysj,@m.xckhndzw,@m.xckhndjb)

*!*			tcjb=LEFT(m.xjb_dc,2)

*!*			v_zwgzdc=ALLTRIM(SUBSTR(m.xjb_dc,3,2))&&倒档处理
*!*			IF VAL(v_zwgzdc)<=VAL(zgdc(ryjbxx.jbgzjb2))
*!*			    tcdc=v_zwgzdc
*!*			    tcddc=""
*!*			ELSE
*!*			    tcdc=zgdc(ryjbxx.jbgzjb2)
*!*			    tcddc=ALLTRIM(STR(VAL(v_zwgzdc)-VAL(zgdc(ryjbxx.jbgzjb2))))
*!*			ENDIF
    ELSE
        m.zwbm=tczwbm
    ENDIF
	    
&&规则1:保持级别最高
	DO case
	CASE VAL(lat[1,2])<VAL(tcjb)&&级别没变且低于学历级别或已生级别(职务变化引起级别晋升):
        m.jb=lat[1,2]
	    IF jbgz06(lat[1,2],lat[1,3],v_tbnd)>=jbgz06(m.tcjb,m.tcdc,v_tbnd)
	        m.dc=lat[1,3]
	    ELSE
	        m.dc=LEFT(jbjs06(tcjb,tcdc,lat[1,2],v_tbnd),2)
	    ENDIF

	CASE VAL(lat[1,2])=VAL(tcjb)
	    m.jb=tcjb
	    m.dc=IIF(VAL(lat[1,3])<VAL(tcdc),tcdc,lat[1,3])
	    
	OTHERWISE
	    m.jb=tcjb
	    IF jbgz06(lat[1,2],lat[1,3],v_tbnd)<=jbgz06(m.tcjb,m.tcdc,v_tbnd)
	        m.dc=m.tcdc
	    ELSE
	        m.dc=LEFT(jbjs06(lat[1,2],lat[1,3],m.jb,v_tbnd),2)
	    ENDIF
	ENDCASE
&&规则2:比较职务和级别工资,以较高者
*!*	    IF zwgz06(tczwbm,v_tbnd)+jbgz06(tcjb,tcdc,v_tbnd)<zwgz06(m.zwbm,v_tbnd)+jbgz06(lat[1,2],lat[1,3],v_tbnd)
*!*	        m.jb=lat[1,2]
*!*	        m.dc=lat[1,3]
*!*	    ELSE
*!*	        m.jb=tcjb
*!*	        m.dc=tcdc
*!*	    ENDIF

&&规则3:只比较级别工资,以较高者
*!*	    IF jbgz06(tcjb,tcdc,v_tbnd)<jbgz06(lat[1,2],lat[1,3],v_tbnd)
*!*	        m.jb=lat[1,2]
*!*	        m.dc=lat[1,3]
*!*	    ELSE
*!*	        m.jb=tcjb
*!*	        m.dc=tcdc
*!*	    ENDIF
    		
CASE INLIST(LEFT(tczwbm,2),"05","06")
	m.zwbm=IIF(lat[1,1]<=tczwbm,lat[1,1],tczwbm)
    m.jb=tcjb
    m.dc=tcdc
    
OTHERWISE
	m.zwbm=tczwbm
    m.jb=""
    m.dc=IIF(VAL(lat[1,3])>VAL(tcdc),lat[1,3],tcdc)
    
ENDCASE

RETURN RIGHT(SPACE(4)+m.zwbm,4)+RIGHT("  "+m.jb,2)+RIGHT("  "+m.dc,2)
