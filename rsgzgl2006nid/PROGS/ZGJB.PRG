FUNCTION zgjb

PARAMETERS tcZwbm

IF !USED("jbtgbz")
    USE jbtgbz IN 0
ENDIF

SEEK tcZwbm ORDER tag zwbm IN jbtgbz
IF !FOUND("jbtgbz")
    RETURN ""
ENDIF

RETURN jbtgbz.zgjb