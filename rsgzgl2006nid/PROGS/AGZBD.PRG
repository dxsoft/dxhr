FUNCTION agzbd

PARAMETERS tcRybm,lagzbd
EXTERNAL ARRAY lagzbd

LOCAL k

SELECT DISTINCT jsnf+"."+jsyf as jssj,jslb,00000 as zze,hj2,bbz FROM hisbase WHERE dwbm+grbm=tcrybm ORDER BY jssj,hj2,bbz INTO ARRAY lagzbd
ygz=0
FOR k=1 TO ALEN(lagzbd,1)
    lagzbd[k,3]=lagzbd[k,4] - ygz
    ygz=lagzbd[k,4]
ENDFOR

RETURN _tally
