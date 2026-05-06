FUNCTION zRound

PARAMETERS nValue

LOCAL nretval

IF cyxx.blxs="0"
    IF cyxx.swyz="1"
        RETURN ROUND(nValue+0.4,0)
    ELSE
        RETURN ROUND(nValue,0)
    ENDIF
ELSE
    IF cyxx.swyz="1"
        nretval=ROUND(nValue+0.04,1)
    ELSE
        nretval=ROUND(nValue,1)
    ENDIF
ENDIF

IF INT(nretval)=nretval
    RETURN INT(nretval)
ELSE
    RETURN nretval
ENDIF