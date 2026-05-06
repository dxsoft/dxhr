FUNCTION arzjl

PARAMETERS tcRybm,tcDwsx,larzjl
EXTERNAL ARRAY larzjl

*!*	SELECT DISTINCT zwmc,zwjb,zwjb,rzsj as srny FROM jdzw WHERE dwbm+grbm=tcrybm UNION ;
*!*	SELECT DISTINCT xrzw,zwjb,xzzw,srny FROM ryzwbh WHERE dwbm+grbm=tcrybm AND LEFT(zjbm,2)=IIF(tcDwsx<"10",tcDwsx,"10") ;
*!*	 ORDER BY srny INTO ARRAY larzjl

SELECT DISTINCT zwmc,zwjb,zwjb,rzsj as srny FROM jdzw WHERE dwbm+grbm=tcrybm UNION ;
SELECT DISTINCT xrzw,zwjb,xzzw,srny FROM ryzwbh WHERE dwbm+grbm=tcrybm ;
 ORDER BY srny INTO ARRAY larzjl


RETURN _tally
