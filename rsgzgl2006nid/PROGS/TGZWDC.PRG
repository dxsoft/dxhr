FUNCTION tgzwdc

PARAMETERS tcZwbm,tnGznx,tnRznx,tcXl,tcTbnd,ttZyjr&&职务编码、任职年限、工作年限、套改时学历、套改年月,转业军人

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
m.zwkhnd=LEFT(tcTbnd,4)
m.tgdc = dctgbz.a&fldname1

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

m.tggzse=zwgzse(tcZwbm,m.tgdc,tcTbnd)

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
*!*		m.zwkhnd="1993"
ENDIF

IF (tcZwbm='0205' OR tcZwbm='0301' OR tcZwbm='0605' OR tcZwbm='0701') and m.tgdc<'2'&&工人最底套入初工2
    m.tgdc='2'
ENDIF

RETURN ALLTRIM(m.zwkhnd)+ALLTRIM(m.tgdc)