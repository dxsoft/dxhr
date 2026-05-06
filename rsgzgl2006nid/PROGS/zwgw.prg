FUNCTION zwgw

PARAMETERS tcdwbm,tcgrbm,tcdwsx,tcdate

SELECT zjbm,zwjb,zwbm,xzzw,srny,xrzw FROM ryzwbh WHERE dwbm=tcdwbm AND grbm=tcgrbm AND LEFT(zwbm,2)=tcdwsx AND srny=tcdate ORDER BY srny DESC INTO ARRAY lsarray
IF _tally>0
    RETURN lsarray[1,1]+","+lsarray[1,2]+","+lsarray[1,3]+","+lsarray[1,4]+","+lsarray[1,5]+","+lsarray[1,6]
ELSE
    RETURN SPACE(100)
ENDIF