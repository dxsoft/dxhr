FUNCTION dwmc

PARAMETERS tcdwbm

SEEK tcdwbm ORDER tag dwbm IN dwbm
IF FOUND("dwbm")
    RETURN dwbm.dwmc
ELSE
    RETURN SPACE(30)
ENDIF