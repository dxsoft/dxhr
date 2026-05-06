FUNCTION andkh

PARAMETERS tcRybm,landkh
EXTERNAL ARRAY landkh

SELECT DISTINCT khnd,khjg FROM ndkh WHERE dwbm+grbm=tcrybm ORDER BY khnd INTO ARRAY landkh

RETURN _tally
