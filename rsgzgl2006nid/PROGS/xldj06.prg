FUNCTION xldj06

PARAMETERS tcTbnd,tczwbm,tcXlbm

IF EMPTY(tcXlbm)
    RETURN ""
ENDIF

LOCAL oldalias,latt

oldalias=SELECT()

IF !USED("bz06_zzdz")
    crtvbz06_zzdz(.f.,conn)
ENDIF

IF tczwbm>"10"
    tczwbm="10"+SUBSTR(tcZwbm,3)
ENDIF

SELECT RIGHT(SPACE(4)+bz06_zzdz.zzzwbm,4)+RIGHT(SPACE(2)+ALLTRIM(bz06_zzdz.zzjb),2)+RIGHT(SPACE(2)+ALLTRIM(bz06_zzdz.zzdc),2) FROM bz06_zzdz WHERE tbnd=tcTbnd AND LEFT(zzzwbm,2)=LEFT(tczwbm,2) AND xlbm=ALLTRIM(tcXlbm) INTO ARRAY latt

SELECT (oldalias)
RETURN latt[1,1]