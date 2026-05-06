FUNCTION jbscope
PARAMETERS tcZwbm

LOCAL latjbs
DIMENSION latjbs[1]

SELECT RIGHT(SPACE(2)+ALLTRIM(STR(VAL(bz06_zw_jb_xj.min))),2)+RIGHT(SPACE(2)+ALLTRIM(STR(VAL(bz06_zw_jb_xj.max))),2) FROM bz06_zw_jb_xj WHERE zwbm=tczwbm INTO ARRAY latjbs
IF _tally>0
    RETURN latjbs[1]
ELSE
    RETURN "    "
ENDIF