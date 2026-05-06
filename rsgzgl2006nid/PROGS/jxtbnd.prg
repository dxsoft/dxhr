&&由当前日期确定调标年度
FUNCTION jxtbnd

PARAMETERS cCurrentDate,cGzbz,cJxlb

LOCAL lcCurrentDate
lcCurrentDate=STRTRAN(cCurrentDate,".","")

SELECT tbnd FROM (cGzbz) WHERE jxlb=cJxlb AND tbnd<=lcCurrentDate DISTINCT INTO ARRAY lsarray order by tbnd DESC

IF _tally>0
    RETURN lsarray[1]
ELSE
    RETURN ""
ENDIF
