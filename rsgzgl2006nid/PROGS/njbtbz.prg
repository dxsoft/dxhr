FUNCTION njbtbz

PARAMETERS tcdwbm

LOCAL liNjbt
DIMENSION liNjbt[1]

SELECT njbt FROM dwbm WHERE dwbm=tcdwbm AND gzczbz="事业管理" INTO ARRAY liNjbt
IF _tally>0
    RETURN liNjbt[1]-1
ELSE
    RETURN 0
ENDIF