FUNCTION tgdc

PARAMETERS tcZwbm,tnGznx,tnRznx,tcTgnf&&职务编码、上任年月、工作年限,返回是否高套('0','1')+档次

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
m.tgdc = dctgbz.a&fldname1

m.zwkhnd='0'

IF tcTgnf="1993" AND !EMPTY(tcTgnf)&&按94年套
	SELECT dctgbz
	LOCATE FOR czbm=m.czbm and betw(m.tnRznx+1,zdrznx,zgrznx) &&行定位
	IF !FOUND("dctgbz")
	    MESSAGEBOX('定资人员套改职务上任时间有误！',64,'系统提示')
	    RETURN ""
	ENDIF

	i=1
	DO WHILE .t.
	    fldname='N'+ALLTRIM(STR(i))
	    IF tnGznx+1 <= VAL(dctgbz.&fldname)
	        EXIT
	    ENDIF
	    i=i+1
	ENDDO

	fldname1=ALLTRIM(STR(i))
	m.tgdc94 = dctgbz.a&fldname1
    IF m.tgdc94>m.tgdc
        m.zwkhnd='1'
        m.tgdc=m.tgdc94
    ENDIF

ENDIF

IF (tcZwbm='0205' OR tcZwbm='0301' OR tcZwbm='0605' OR tcZwbm='0701') and m.tgdc<'2'&&工人最底套入初工2
    m.tgdc='2'
ENDIF

RETURN ALLTRIM(m.zwkhnd)+ALLTRIM(m.tgdc)