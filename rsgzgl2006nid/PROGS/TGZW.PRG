FUNCTION tgzwdc

PARAMETERS tcZwbm,tnGznx,tnRznx

IF (tnRznx=5 or tnRznx=10)
    tnRznx=tnRznx+1
ENDIF

SEEK "001"+tcZwbm ORDER tag bm IN dmb

IF !FOUND("dmb")
    MESSAGEBOX('定资人员93套改时职务信息有误！',64,'系统提示')
    RETURN ""
ELSE
    m.czbm=dmb.czbm
ENDIF

SELECT dctgbz
LOCATE FOR czbm=m.czbm and betw(m.tnRznx,zdrznx,zgrznx) &&行定位
IF !FOUND("dctgbz")
    MESSAGEBOX('定资人员套改职务上任时间有误！',64,'系统提示')
    RETURN ""
ENDIF

i=1
DO WHILE .t.
    fldname='N'+ALLTRIM(STR(i))
    IF tnGznx <= VAL(dctgbz.&fldname)
        EXIT
    ENDIF
    i=i+1
ENDDO

fldname1=ALLTRIM(STR(i))
m.zwkhnd='1993'
m.tgdc = dctgbz.a&fldname1

IF tnGznx = VAL(dctgbz.&fldname)&&高套一档
    fldname=ALLTRIM(STR(i+1))
    IF dctgbz.a&fldname > dctgbz.a&fldname1
        m.zwkhnd='1994'
        m.tgdc=dctgbz.a&fldname
    ENDIF
ENDIF

&&比较

IF EMPTY(tcXl)
    m.xlgzse=0
ELSE
	SELECT xldzbz
	IF ttZyjr
	    LOCATE FOR xl=tcXl and LEFT(zwbm,2)='01'
	ELSE
	    LOCATE FOR xl=tcXl and LEFT(zwbm,2)=LEFT(tcZwbm,2)
	ENDIF

    m.xlgzse=zwgzse(xldzbz.Zwbm,xldzbz.gzdc,tcTbnd)
ENDIF

m.tggzse=zwgzse(tcZwbm,m.tgdc,"199307")

IF m.tggzse < m.xlgzse &&如低于学历工资应就近就高套入高n档
    SEEK tcTbnd+tcZwbm ORDER ndbm IN zwgzbz
    IF FOUND('zwgzbz')
	    FOR j=1 to 16
	        v_j=allt(str(j))
	        IF m.xlgzse<=zwgzbz.a&v_j
	            m.tgdc=v_j
	            m.tggzse=zwgzbz.a&v_j 
	            EXIT
	        ENDIF
	    endf
	ENDIF
	m.zwkhnd="1993"
ENDIF

IF tcZwbm='0205' and m.tgdc<'2'&&工人最底套入初工2
    m.tgdc='2'
ENDIF

RETURN ALLTRIM(m.zwkhnd)+ALLTRIM(m.tgdc)