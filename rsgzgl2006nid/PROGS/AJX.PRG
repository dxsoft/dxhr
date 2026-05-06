FUNCTION ajx

PARAMETERS tcRybm,lajx
EXTERNAL ARRAY lajx

SELECT DISTINCT jx,sysj FROM jx WHERE dwbm+grbm=tcrybm ORDER BY sysj INTO ARRAY lajx

RETURN _tally
