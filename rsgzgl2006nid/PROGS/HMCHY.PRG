FUNCTION hmchy

PARAMETERS v_wxtb

v_dwjc=m.dwjc
v_yjslb=ryjbxx.jslb

v_jsbj=v_rybm+ryjbxx.jsnf+ryjbxx.jsyf+ryjbxx.jslb

SELECT hisbase
SEEK v_jsbj ORDER tag yzbm IN hisbase
IF !FOUND("hisbase")
    MESSAGEBOX(ryjbxx.grbm+ryjbxx.xm+CHR(13)+CHR(10)+CHR(13)+CHR(10)+'无历史信息, 无法还原!',0+48,'系统提示',10)
    RETURN .F.
ELSE
    SELECT hisbase
    SCATTER MEMVAR

    SELECT ryjbxx
    GATHER MEMVAR

    SELECT hisbase
    REPLACE yznf WITH "",yzyf WITH "",yzjslb WITH "" IN hisbase
    
    SEEK v_jsbj ORDER tag bmlb IN hisbase
    IF FOUND("hisbase")
        DELETE IN hisbase
    ENDIF

    IF USED('ryzwbh') and ryjbxx.dwsx>='10'
        SELECT ryzwbh
        LOCATE FOR dwbm+grbm=v_rybm and xrzwbz='1' and jsbz='1'
        REPLACE jsbz WITH '' 
    ENDIF
    
    IF USED('ryzwbh')
        IF v_yjslb='聘干定资' OR (v_yjslb='学历变化' and ryjbxx.zwgw2<>ryzwbh.xzzw)
            SELECT ryzwbh
            SKIP -1
            REPLACE xrzwbz with '1' in ryzwbh
            SKIP 
            DELETE in ryzwbh 
        ENDIF
    ENDIF
    
    
    RETURN .T.
	
ENDIF 

oldalias=ALIAS()

SELECT dmb
LOCATE FOR bm="001"+ALLTRIM(ryjbxx.dwsx) AND LEN(bm)=5
REPLACE ryjbxx.gwfl WITH dmb.mc IN ryjbxx
SELECT (oldalias)