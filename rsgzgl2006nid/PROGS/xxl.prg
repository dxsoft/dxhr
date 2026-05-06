FUNCTION xXl

PARAMETERS tcRybm

LOCAL oldOrder,oldAlias

oldalias=ALIAS()
SELECT dxl
m.oldorder=SET("Order")

SET FILTER TO xllb<>"ÆäËü" AND dwbm+grbm=tcRybm IN xl
SET ORDER TO tag bysj IN xl
GO BOTTOM in xl

SET ORDER TO (oldOrder) IN xl
SET FILTER TO IN xl
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF

IF !EOF('xl')
    RETURN xl.bysj+xl.xl
ELSE
    RETURN ""
ENDIF
