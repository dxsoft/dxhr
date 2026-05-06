FUNCTION jxbl

PARAMETERS tcdwbm

SEEK tcdwbm ORDER tag dwbm IN dwbm
IF FOUND("dwbm")
    RETURN dwbm.jxbl
ELSE
    RETURN "     "
ENDIF
