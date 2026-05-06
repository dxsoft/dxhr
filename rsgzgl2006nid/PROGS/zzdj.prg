FUNCTION zzdj

PARAMETERS tcTbnd,tcdwsx,tcXlbm

DO case
CASE tcdwsx="05" OR tcdwsx="08"
	v_zwbm=tcdwsx+"05"
	v_zwgzdc="2"
	v_jbgzjb=""

CASE tcdwsx="06" OR tcdwsx="09"
	v_zwbm=tcdwsx+"01"
	v_zwgzdc="2"
	v_jbgzjb=""
   
OTHERWISE
	IF EMPTY(tcXlbm)
	    RETURN ""
	ENDIF
	DO CASE
	CASE tcXlbm="11"
	    v_xl="博士"
	CASE tcXlbm="12"
	    v_xl="硕士"
	CASE INLIST(tcXlbm,"13","14","15","19")
	    v_xl="研究生"
	CASE tcXlbm="21"
	    v_xl="双学士"
	CASE INLIST(tcXlbm,"22","23","24","28","29")
	    v_xl="本科"
	CASE tcXlbm="30"
	    v_xl="大普"
	CASE INLIST(tcXlbm,"31","32","38","39")
	    v_xl="专科"
	CASE tcXlbm="41"
	    v_xl="中专"
	CASE INLIST(tcXlbm,"42","49")
	    v_xl="中技"
	CASE INLIST(tcXlbm,"62","69",'51')
	    v_xl="高中"
	CASE tcXlbm="61"
	    v_xl="职高"
	CASE tcXlbm="71"
	    v_xl="初中"
	OTHERWISE
	    v_xl="999999"
    ENDCASE

	IF tcdwsx>='10'
	    tcdwsx='15'&&未考虑高校
	ENDIF

	SELECT xldzbz
	LOCATE FOR LEFT(zwbm,2)=tcdwsx AND ALLTRIM(xl)=ALLTRIM(v_Xl)
    IF !FOUND("xldzbz")
        RETURN ""
    ENDIF
    
	v_zwbm=xldzbz.zwbm
	IF LEFT(v_zwbm,2)='07' AND SUBSTR(v_zwbm,4,1)="0"
	    v_zwbm="070"+SUBSTR(v_zwbm,3,1)
	ENDIF
	v_zwgzdc=xldzbz.gzdc
	v_jbgzjb=xldzbz.gzjb
	
ENDCASE

RETURN RIGHT(SPACE(4)+v_zwbm,4)+RIGHT(SPACE(2)+ALLTRIM(v_jbgzjb),2)+RIGHT(SPACE(2)+ALLTRIM(v_zwgzdc),2)
