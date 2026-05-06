&&现任职务
FUNCTION xzxzw

PARAMETERS cRybm,cCurrentDate

LOCAL latzw
*!*	SELECT zwbm2 FROM hisbase WHERE dwbm+grbm=crybm AND jsnf+jsyf<=cCurrentDate AND bbz<>"模拟推算" ORDER BY jsnf desc,jsyf DESC INTO ARRAY lazw
SELECT IIF(AT("F",zwbm2)>0,LEFT(zwbm2,3)+"F",zwbm2) FROM hisbase WHERE dwbm+grbm=crybm AND jsnf+jsyf<=cCurrentDate ORDER BY jsnf desc,jsyf DESC INTO ARRAY lazw
IF _tally>0
    RETURN lazw[1,1]
ELSE
    return"    "
ENDIF
