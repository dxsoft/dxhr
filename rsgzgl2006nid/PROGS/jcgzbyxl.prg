FUNCTION jcgzbyxl

PARAMETERS tcxlbm

SELECT bz FROM bz_jcgz WHERE xlbm=tcxlbm INTO ARRAY lat
IF _tally>0
    RETURN lat[1]
ELSE
    RETURN 0000
ENDIF
