FUNCTION srny

PARAMETERS tcbm

oldalias=ALIAS()
SEEK tcbm ORDER tag bm IN dryzwbh
IF FOUND("dryzwbh")
    RETURN dryzwbh.srny
ELSE
    RETURN SPACE(7)
ENDIF

