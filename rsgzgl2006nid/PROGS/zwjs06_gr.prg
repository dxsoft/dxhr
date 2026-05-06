FUNCTION zwjs06_gr

PARAMETERS tcyzwbm,tcyDc,tcDjc,tczwbm,tcTbnd

IF m.zwbhhjsdj='¡Ì'
    v_add=jsdjgz06(tczwbm,tctbnd)-jsdjgz06(tcyzwbm,tctbnd)
ELSE
    v_add=0
ENDIF

v_yzwgz=zwgz06_gr(tcyzwbm,tcydc,tcDjc,tcTbnd)
i=1
DO WHILE .T.
    IF zwgz06_gr(tczwbm,ALLTRIM(STR(i)),tcDjc,tcTbnd)+v_add>v_yzwgz
        EXIT
    ELSE
        i=i+1
    ENDIF
ENDDO

RETURN ALLTRIM(STR(i))