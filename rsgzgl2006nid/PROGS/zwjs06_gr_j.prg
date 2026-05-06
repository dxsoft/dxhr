FUNCTION zwjs06_gr_j

PARAMETERS tcyzwbm,tcyDc,tczwbm,tcTbnd

LOCAL kk,czwbm,yzwgz,i

IF m.zwbhhjsdj='¡Ì'
    v_add=jsdjgz06(tczwbm,tctbnd)-jsdjgz06(tcyzwbm,tctbnd)
ELSE
    v_add=0
ENDIF

v_yzwgz=zwgz06_gr(tcyzwbm,tcydc,'',tcTbnd)
i=VAL(tcydc)

FOR kk=1 TO VAL(tczwbm)-VAL(tcyzwbm)
    czwbm="0"+ALLTRIM(STR(VAL(tcyzwbm)+kk))
	yzwgz=zwgz06_gr(tcyzwbm,ALLTRIM(STR(i)),'',tcTbnd)
	DO WHILE .T.
	    IF zwgz06_gr(czwbm,ALLTRIM(STR(i)),'',tcTbnd)+v_add>v_yzwgz
	        EXIT
	    ELSE
	        i=i+1
	    ENDIF
	ENDDO
ENDFOR

RETURN ALLTRIM(STR(i-1))