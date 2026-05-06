FUNCTION zzdj06

PARAMETERS tcTbnd,tczwbm,tcXlbm

LOCAL gddc

IF EMPTY(tcXlbm)
    RETURN ""
ENDIF

LOCAL oldalias

oldalias=SELECT()

IF !USED("bz06_zzdz")
    crtvbz06_zzdz(.f.,conn)
ENDIF
SELECT bz06_zzdz 

IF tczwbm>"10" AND LEFT(tczwbm,2)<>"21" AND LEFT(tczwbm,2)<>"22" AND LEFT(tczwbm,2)<>"23" AND LEFT(tczwbm,2)<>"24" AND LEFT(tczwbm,2)<>"25" AND LEFT(tczwbm,2)<>"26" AND LEFT(tczwbm,2)<>"27" AND LEFT(tczwbm,2)<>"28"
    tczwbm="10"+SUBSTR(tcZwbm,3)
ENDIF

IF LEFT(tczwbm,2)="03" OR LEFT(tczwbm,2)="04"
    tczwbm="01"+SUBSTR(tczwbm,3)
ENDIF

*!*	IF INLIST(LEFT(tczwbm,2),"21","22","23","24","25","26","27","28") AND tcTbnd<="201807"&&之前转正按（含201807，2023.09.27改）
*!*	    tczwbm="01"+SUBSTR(tczwbm,3)
*!*	ENDIF

*!*	LOCATE FOR tbnd=tcTbnd AND LEFT(zzzwbm,2)=LEFT(tczwbm,2) AND xlbm=ALLTRIM(tcXlbm)

LOCATE FOR LEFT(zzzwbm,2)=LEFT(tczwbm,2) AND xlbm=ALLTRIM(tcXlbm)&&2006工改后转正定级标准与工资标准调整无关，去掉工资标准限制

SELECT (oldalias)

IF FOUND("bz06_zzdz")
    IF tcxlbm<"60"
        IF m.zzgddchgr
            m.gddc=m.zzgddc
        ELSE
            IF !INLIST(LEFT(tczwbm,2),"05","06","08","09")
                m.gddc=m.zzgddc
            ELSE
                m.gddc=0
            ENDIF
        ENDIF
    ELSE
        m.gddc=0
    ENDIF
    
    tczwbm=RIGHT(SPACE(4)+bz06_zzdz.zzzwbm,4)        
    IF LEFT(tczwbm,2)='07' AND SUBSTR(tczwbm,4,1)="0"
        tczwbm="070"+SUBSTR(tczwbm,3,1)
    ENDIF
    
    RETURN tczwbm+RIGHT(SPACE(2)+ALLTRIM(bz06_zzdz.zzjb),2)+RIGHT(SPACE(2)+ALLTRIM(STR(VAL(bz06_zzdz.zzdc)+m.gddc)),2)
ENDIF

RETURN SPACE(8)
