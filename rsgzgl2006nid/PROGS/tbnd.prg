&&由当前日期确定调标年度
FUNCTION tbnd

PARAMETERS cCurrentDate,cGzbz

LOCAL lcCurrentDate
lcCurrentDate=STRTRAN(cCurrentDate,".","")
DO case
CASE UPPER(cGzbz)="JXJTBZ"
    SELECT tbnd FROM jxjtbz WHERE UPPER(lb)="JX" DISTINCT INTO ARRAY lsarray order by tbnd
CASE UPPER(cGzbz)="JCJTBZ"
    SELECT tbnd FROM jxjtbz WHERE UPPER(lb)="JC" DISTINCT INTO ARRAY lsarray order by tbnd
CASE UPPER(cGzbz)="SPJTBZ"
    SELECT tbnd FROM jxjtbz WHERE UPPER(lb)="SP" DISTINCT INTO ARRAY lsarray order by tbnd
CASE UPPER(cGzbz)="MTJTBZ"
    SELECT tbnd FROM jxjtbz WHERE UPPER(lb)="MT" DISTINCT INTO ARRAY lsarray order by tbnd
OTHERWISE
    SELECT tbnd FROM (cGzbz) DISTINCT INTO ARRAY lsarray order by tbnd
ENDCASE

IF _tally>0
	cTbnd=lsarray[1]

	FOR i=2 to alen(lsarray,1) 
	    if lcCurrentDate < lsarray[i]
	        EXIT
	    else
	        cTbnd=lsarray[i]
	    endif
	ENDFOR
ELSE
    cTbnd=""
ENDIF
RETURN ALLTRIM(cTbnd)