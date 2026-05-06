FUNCTION djjstj

PARAMETERS tczwbm,tngznx,tcxdj

select dc from bz06_tgb where zwbm=tczwbm and BETWEEN(tngznx,tgnxs,tgnxz) INTO ARRAY latdj

IF VAL(tcxdj)=VAL(latdj[1])
    RETURN 0
ELSE
    IF VAL(tcxdj)>VAL(latdj[1])
        RETURN -1
    ELSE
        RETURN VAL(latdj[1])
    ENDIF
ENDIF
    
