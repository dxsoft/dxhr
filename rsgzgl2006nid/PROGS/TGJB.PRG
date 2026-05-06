FUNCTION tgjb

PARAMETERS tcZwbm,tnGznx,tcXl,tcTgnf &&tnGznx 包含学历年限

**由工龄计算级别
SEEK tcZwbm ORDER tag zwbm IN jbtgbz
IF !FOUND("jbtgbz")
    RETURN ""
ENDIF

i=1
DO WHILE .t.
    fldname='N'+ALLTRIM(STR(i))
    IF tnGznx < VAL(jbtgbz.&fldname)
        EXIT
    ENDIF
    i=i+1
ENDDO
i = i-1
fldname="a"+ALLTRIM(STR(i,2,0))
m.tgjb=jbtgbz.&fldname
m.jbkhnd = tcTgnf

**判断工作年限算到下一年能否高套
IF m.jbkhnd="1993" AND !EMPTY(m.jbkhnd)
	i=1
	DO WHILE .t.
	    fldname='N'+ALLTRIM(STR(i))
	    IF tnGznx+1 < VAL(jbtgbz.&fldname)
	        EXIT
	    ENDIF
	    i=i+1
	ENDDO
	i = i-1
	fldname="a"+ALLTRIM(STR(i,2,0))
	m.tgjb94=jbtgbz.&fldname
	IF VAL(m.tgjb94)<VAL(m.tgjb)
	    m.tgjb=m.tgjb94
    	m.jbkhnd = "1994"
    ENDIF
ENDIF

**按学历套
IF !EMPTY(tcXl)
	SELECT xldzbz
	LOCATE FOR xl=ALLTRIM(tcXl) and SUBSTR(zwbm,1,2)=LEFT(tcZwbm,2)
	v_gzjb=gzjb
	IF VAL(m.tgjb) > val(v_gzjb)
	    m.tgjb=v_gzjb
	    m.jbkhnd=tcTgnf
	ENDIF
ENDIF

RETURN ALLTRIM(m.jbkhnd)+ALLTRIM(m.tgjb)
