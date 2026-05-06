FUNCTION zwgztg
PARAMETERS tcRybm,tcTgny,tcCjgzny,tnZdgznx,tnBjglxlnx,tcXzwbm,tcXzwmc,tcXzwsrny,tcXzwdc,tnXzwgzse,tcDzwbm,tcDzwmc,tcDzwsrny,tcDzwdc,tnDzwgzse,tcXl,tcXlzwbm,tcXlzwmc,tcXlzwdc,tnXlgzse,tcTgzwdc,tnTggzse,tcXckhnd

PRIVATE v_xrznx,v_xrznx,v_sfgt,v_dsfgt,v_drznx,v_gznx,v_tbnd,v_tag

tcXl=SUBSTR(xl(tcRybm,tcTgny),7)

v_tbnd=tbnd(tcTgny,"zwgzbz")
**任职年限
v_xrznx=VAL(LEFT(tcTgny,4))-VAL(LEFT(tcXzwsrny,4))+1

**低一级职务任职年限
v_drznx=VAL(LEFT(tcTgny,4))-VAL(LEFT(tcDzwsrny,4))+1

**工作年限
v_gznx=VAL(LEFT(tcTgny,4))-VAL(LEFT(tcCjgzny,4))+1-tnZdgznx
IF cyxx.tgdcgljs='√'
    v_gznx=v_gznx + tnBjglxlnx
ENDIF

**套改
tcXzwdc=tgdc(tcXzwbm,v_gznx,v_xrznx,LEFT(tcTgny,4))&&现职务套

v_sfgt=LEFT(tcXzwdc,1)
tcXzwdc=ALLTRIM(SUBSTR(tcXzwdc,2))
tcTgzwdc=tcXzwdc
tnXzwgzse=zwgzse(tcXzwbm,tcXzwdc,v_tbnd)

IF !EMPTY(tcDzwbm)&&低职务套
    tcDzwdc=tgdc(tcDzwbm,v_gznx,v_drznx,LEFT(tcTgny,4))
    v_dsfgt=LEFT(tcDzwdc,1)
    tcDzwdc=ALLTRIM(SUBSTR(tcDzwdc,2))
    tnDzwgzse=zwgzse(tcDzwbm,tcDzwdc,v_tbnd)
ENDIF

IF !EMPTY(tcXl)
	SELECT xldzbz&&学历套
    LOCATE FOR xl=ALLTRIM(tcXl) AND LEFT(zwbm,2)=LEFT(tcXzwbm,2)
    tcXlzwmc=xldzbz.zwmc
    tcXlzwbm=xldzbz.zwbm
    tcXlzwdc=ALLTRIM(xldzbz.gzdc)
    tnXlgzse=zwgzse(xldzbz.Zwbm,xldzbz.gzdc,v_Tbnd)
ENDIF

&&比较
v_tag="zw"
tnTggzse=tnXzwgzse
IF tnXlgzse>tnTggzse
    IF tnXlgzse>tnDzwgzse
        v_tag="xl"
        tnTggzse=tnXlgzse
    ELSE
        v_tag="dzw"
        tnTggzse=tnDzwgzse
    ENDIF
ELSE
    IF tnDzwgzse>tnTggzse
        v_tag="dzw"
        tnTggzse=tnDzwgzse
    ENDIF
ENDIF

IF v_tag<>"zw"&&就近就高套入高n档
    SEEK v_Tbnd+tcXzwbm ORDER ndbm IN zwgzbz
    IF FOUND('zwgzbz')
	    FOR j=1 to 16
	        v_j=allt(str(j))
	        IF tnTggzse<=zwgzbz.a&v_j
	            tcTgzwdc=v_j
	            tnTggzse=zwgzbz.a&v_j 
	            EXIT
	        ENDIF
	    endf
	ENDIF
ENDIF

tcXckhnd=LEFT(tcTgny,4)
IF v_tag="zw"
    tcXckhnd=ALLTRIM(STR(VAL(LEFT(tcTgny,4))+VAL(v_sfgt)))
ENDIF
IF v_tag="dzw"
    tcXckhnd=ALLTRIM(STR(VAL(LEFT(tcTgny,4))+VAL(v_dsfgt)))
ENDIF

IF ALLTRIM(tcTgzwdc)=ALLTRIM(tcXlzwdc)&&套改档次等于学历档次，则取消高套年限
    tcXckhnd=LEFT(tcTgny,4)
ENDIF
