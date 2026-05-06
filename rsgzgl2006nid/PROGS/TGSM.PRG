FUNCTION tgsm

PARAMETERS tcdwgrbm

SELECT describe FROM tgxx WHERE dwbm+grbm=tcdwgrbm INTO ARRAY lat

IF _tally>0
    RETURN lat[1]
ELSE
    RETURN ""
ENDIF
