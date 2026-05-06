FUNCTION jbjs06&&级别晋升

PARAMETERS tnyJb,tcyDc,tcJb,tcTbnd

v_yjbgz=jbgz06(tnyJb,tcyDc,tcTbnd)

m.v_jb=tnyJb
m.v_dc=tcyDc
m.v_cdc="0"
IF VAL(tnyJb)>VAL(tcJb)&&晋升
    FOR j=1 TO VAL(tnyJb)-VAL(tcJb)
		i=1
		DO WHILE .T.
		    IF jbgz06(ALLTRIM(STR(VAL(v_jb)-1)),ALLTRIM(STR(i)),tcTbnd)>v_yjbgz
		        EXIT
		    ELSE
		        i=i+1
		    ENDIF
		ENDDO
		v_yjbgz=jbgz06(ALLTRIM(STR(VAL(v_jb)-1)),ALLTRIM(STR(i)),tcTbnd)
		IF jbgz06(ALLTRIM(STR(VAL(v_jb)-1)),ALLTRIM(STR(i)),tcTbnd)>jbgz06(m.v_jb,ALLTRIM(STR(VAL(m.v_dc)+1)),tcTbnd)&&超档差
		    m.v_cdc="1"
		ENDIF
		m.v_dc=ALLTRIM(STR(i))
        m.v_jb=ALLTRIM(STR(VAL(v_jb)-1))
    ENDFOR
ENDIF

IF VAL(tnyJb)<VAL(tcJb)&&降级
    FOR j=1 TO VAL(tcJb)-VAL(tnyJb)
		i=0
		DO WHILE .T.
		    IF jbgz06(ALLTRIM(STR(VAL(v_jb)+1)),ALLTRIM(STR(i+1)),tcTbnd)>v_yjbgz
		        EXIT
		    ELSE
		        i=i+1
		    ENDIF
		ENDDO
		v_yjbgz=jbgz06(ALLTRIM(STR(VAL(v_jb)+1)),ALLTRIM(STR(i)),tcTbnd)
		m.v_dc=ALLTRIM(STR(i))
        m.v_jb=ALLTRIM(STR(VAL(v_jb)+1))
    ENDFOR
ENDIF

IF VAL(tnyJb)=VAL(tcJb)&&级别不变
    i=VAL(tcyDc)
ENDIF

RETURN RIGHT(SPACE(2)+ALLTRIM(STR(i)),2)+m.v_cdc