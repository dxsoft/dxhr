FUNCTION ndkhjg

*!*	PARAMETERS tcrybm,lakhjg

*!*	EXTERNAL ARRAY lakhjg

*!*	SELECT khnd,khjg FROM dndkh WHERE dwbm+grbm=tcrybm ORDER BY khnd INTO ARRAY lakhjg
PARAMETERS tcrybm,lakhjg

SELECT khnd,khjg FROM dndkh WHERE dwbm+grbm=tcrybm ORDER BY khnd INTO ARRAY lakhjg
RETURN _tally
