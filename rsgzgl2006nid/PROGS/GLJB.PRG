FUNCTION gljb

PARAMETERS tcZwbm,tnGznx

IF !USED("jbtgbz")
    USE jbtgbz IN 0
ENDIF

SEEK tcZwbm ORDER tag zwbm IN jbtgbz
IF !FOUND("jbtgbz")
    RETURN ""
ENDIF

i = IIF(MOD(tnGznx,5)=0,INT(tnGznx/5),INT(tnGznx/5)+1)
IF i>9
    i=9
ENDIF

fldname="a"+ALLTRIM(STR(i,2,0))

v_jb=ALLTRIM(jbtgbz.&fldname) &&取出对应级别

RETURN v_jb