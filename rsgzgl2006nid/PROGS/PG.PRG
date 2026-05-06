FUNCTION pg&&

PARAMETERS tcZwbm,tnGznx,tnRznx,tcPgny&&职务编码、任职年限、工作年限、聘干年月

LOCAL lcZwbm
&&按低一级职务套
IF RIGHT(ALLTRIM(tcZwbm),2)="14"
    lcZwbm=LEFT(ALLTRIM(tcZwbm),2)+"16"
ELSE
    lcZwbm=LEFT(ALLTRIM(tcZwbm),2)+RIGHT("0"+ALLTRIM(STR(VAL(RIGHT(ALLTRIM(tcZwbm),2))+1)),2)
ENDIF

SEEK "001"+lcZwbm ORDER tag bm IN dmb

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

m.tgdc=zwbhjs(lcZwbm,dctgbz.a&fldname1,tcZwbm,tcPgny)

RETURN m.tgdc