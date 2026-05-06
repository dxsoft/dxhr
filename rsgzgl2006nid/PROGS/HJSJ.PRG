FUNCTION hjsj

PARAMETERS tcdwbm,tcgrbm,tcjssj

SEEK tcdwbm+tcgrbm+tcjssj ORDER tag bm IN hjxx
IF FOUND("hjxx")
    RETURN hjxx.hjsj
ELSE
    RETURN ""
ENDIF
