FUNCTION jjjy06

PARAMETERS tcZwbm,tnGznx,tcCjgzny,tnZdgznx,tcdwbm,tcgrbm,tcdwsx

PRIVATE v_zwbm

IF cyxx.jjjy=1&&截至93年
    v_Zwbm=zwbm93(tcdwbm,tcgrbm,tcdwsx)
ELSE
    v_zwbm=tczwbm
ENDIF

IF EMPTY(v_zwbm)
    RETURN 00
ENDIF

IF cyxx.jjjy=3&&随职务工龄变
    v_gznx=tnGznx
ELSE
    v_gznx=1993-val(left(tcCjgzny,4))+1
    IF v_gznx>tnZdgznx+1
        v_gznx=v_gznx-tnZdgznx
    ENDIF
    
    IF v_gznx<1 OR tcCjgzny>'1993.10.01'
        v_gznx=1
    ENDIF
ENDIF

IF v_Zwbm>"1000"&&专业技术人员
    v_Zwbm='10'+RIGHT(v_Zwbm,2)
ENDIF

IF LEFT(v_Zwbm,2)='05' OR LEFT(v_Zwbm,2)='06' OR LEFT(v_Zwbm,2)='08' OR LEFT(v_Zwbm,2)='09'&&工人
    i=IIF(INT((v_gznx+5)/10)+1<5,INT((v_gznx+5)/10)+1,5)

ELSE
    i=IIF(INT(v_gznx/10)+1<4,INT(v_gznx/10)+1,4)

ENDIF

SELECT bz06_jjjy
LOCATE FOR zwbm=v_Zwbm &&行定位

IF FOUND()
    fldname=ALLTRIM(STR(i,1))
    RETURN bz06_jjjy.a&fldname 
ELSE
    RETURN 00
ENDIF
