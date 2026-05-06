FUNCTION andkhjg

PARAMETERS tcRybm,tcnd,landkh
EXTERNAL ARRAY landkh

SELECT DISTINCT TOP 5 khnd,khjg FROM ndkh WHERE dwbm+grbm=tcrybm AND INLIST(khjg,"优秀","称职","合格") AND khnd<tcnd ORDER BY khnd DESC INTO ARRAY landkh

RETURN _tally
