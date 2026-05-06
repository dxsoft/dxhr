FUNCTION jxlb

PARAMETERS tcdwbm

SEEK tcdwbm ORDER tag dwbm IN dwbm
IF FOUND("dwbm")
    RETURN dwbm.jxlb
ELSE
    RETURN 0
ENDIF