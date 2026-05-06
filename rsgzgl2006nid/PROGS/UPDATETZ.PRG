FUNCTION updatetz

LPARAMETERS tcjsnf,tcjsyf,tcjslb

oldalias=ALIAS()
SELECT ryjbxx
SCATTER MEMVAR
SELECT gzgltz
SEEK ryjbxx.dwbm+ryjbxx.grbm+ryjbxx.jsnf+ryjbxx.jsyf+ryjbxx.jslb ORDER tag bmlb IN gzgltz
IF !FOUND("gzgltz")
    INSERT INTO gzgltz FROM MEMVAR 
ELSE
    GATHER MEMVAR
ENDIF
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF
