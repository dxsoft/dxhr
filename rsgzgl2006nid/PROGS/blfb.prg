FUNCTION blfb

PARAMETERS tczwbm

IF AT("F",tczwbm)>0
    tczwbm=LEFT(tczwbm,3)+"F"
ENDIF

SEEK tczwbm ORDER tag zwbm IN bz06_blfb

IF FOUND("bz06_blfb")
    RETURN bz06_blfb.bz
ELSE
    RETURN 00
ENDIF
