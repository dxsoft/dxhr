FUNCTION prtv
PARAMETERS tvalue
IF TYPE("tvalue")="C"
    IF EMPTY(tvalue)
        RETURN "！！"
    ELSE
        RETURN tvalue
    ENDIF
ENDIF
IF TYPE("tvalue")="N"
    IF tvalue=0
        RETURN "！！"
    ELSE
        RETURN tvalue
    ENDIF
ENDIF