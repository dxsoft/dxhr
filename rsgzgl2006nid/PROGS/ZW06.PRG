FUNCTION zw06

PARAMETERS tcRybm,tcDwsx

SELECT DISTINCT zjbm,zwjb,srny FROM ryzwbh WHERE dwbm+grbm=tcrybm AND LEFT(zjbm,2)=IIF(tcDwsx<"10",tcDwsx,"10") AND ;
STRTRAN(srny,".","")<"200607" AND !EMPTY(srny) AND srny<>"    .  " ORDER BY srny DESC INTO ARRAY lat

cret=""
IF _tally>0
    cret=cret+lat[1,1]+","+lat[1,2]+","+lat[1,3]+","
    RETURN LEFT(cret+SPACE(200),200)
ELSE
    RETURN SPACE(200)
ENDIF