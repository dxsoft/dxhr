PARAMETERS tcRybm,tcKhnd

SEEK tcKhnd +tcRybm ORDER tag ndbm IN ndkh
IF FOUND("ndkh")
    RETURN ndkh.khjg
ELSE
    RETURN ""
ENDIF
