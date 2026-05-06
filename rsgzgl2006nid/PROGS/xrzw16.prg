FUNCTION xrzw16

PARAMETERS tcRybm,tcDwsx,tczjbm

SELECT zjbm,srny FROM ryzwbh WHERE dwbm+grbm=tcRybm AND zjbm=tczjbm ORDER BY zjbm,srny INTO ARRAY laXrzw

IF _tally>0
    RETURN laXrzw[1,1]+laXrzw[1,2]
ELSE
    RETURN "           "
ENDIF
