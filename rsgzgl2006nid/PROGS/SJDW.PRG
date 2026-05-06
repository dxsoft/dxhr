FUNCTION sjdw

PARAMETERS tcdwbm,tcgrbm,tcjssj

SEEK tcdwbm+tcgrbm+tcjssj ORDER tag bm IN hjxx
IF FOUND("hjxx")
    RETURN hjxx.sjdw
ELSE
    RETURN ""
ENDIF
