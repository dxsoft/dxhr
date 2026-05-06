FUNCTION axl

PARAMETERS tcRybm,laxl
EXTERNAL ARRAY laxl

SELECT DISTINCT xl,byyx,bysj,xllb,xz FROM xl WHERE dwbm+grbm=tcrybm ORDER BY bysj INTO ARRAY laxl

RETURN _tally
