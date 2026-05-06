FUNCTION jtbz

PARAMETERS tcdwbm

SEEK tcdwbm ORDER tag dwbm IN dwbm
IF FOUND("dwbm")
    RETURN dwbm.jtbz
ELSE
    RETURN ""
ENDIF
