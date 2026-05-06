FUNCTION xrzw

PARAMETERS tcRybm,tcDwsx

SELECT zwbm,srny FROM ryzwbh ORDER BY srny DESC,zwbm WHERE dwbm+grbm=tcRybm AND LEFT(zwbm,2)=tcDwsx INTO ARRAY laXrzw&&转正当月职务变化，srny同，此时再根据编码排序
IF _tally>0
    RETURN laXrzw[1,1]+laXrzw[1,2]
ELSE
    RETURN "     "
ENDIF
