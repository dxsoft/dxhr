FUNCTION jfly

PARAMETERS tcdwbm

SEEK tcdwbm ORDER tag dwbm IN dwbm
IF FOUND("dwbm")
    RETURN dwbm.jfly
ELSE
    RETURN "        "
ENDIF
