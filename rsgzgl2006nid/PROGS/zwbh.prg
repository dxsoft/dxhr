FUNCTION zwbh

PARAMETERS tdwbm,tgrbm,tjslb,tjsnf,tjsyf

LOCAL latzwbh
DIMENSION latzwbh[1]

IF tjslb="津贴变化"
    SELECT zwgw1,zwgw2,jsnf,jsyf FROM hisbase WHERE dwbm=tdwbm AND grbm=tgrbm AND jslb="职务变化" AND jsnf+jsyf>tjsnf+tjsyf INTO ARRAY latzwbh
    IF _tally>0
        RETURN latzwbh[1,3]+"年"+latzwbh[1,4]+"月 由"+ALLTRIM(latzwbh[1,1])+" 晋升为 "+ALLTRIM(latzwbh[1,2])
    ELSE
        RETURN ""
    ENDIF
ELSE
    RETURN ""
ENDIF
