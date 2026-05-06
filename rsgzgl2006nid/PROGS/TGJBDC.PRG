FUNCTION tgjbdc

PARAMETERS tcZwbm,tcRzsj,tcCjgzsj,tcTgsj&&职务层次编码、任职年限、工作年限、套改时间

SEEK "001"+tcZwbm ORDER tag bm IN dmb

IF !FOUND("dmb")
    MESSAGEBOX('套改时职务信息有误！',64,'系统提示')
    RETURN ""
ELSE
    m.czbm=dmb.czbm
ENDIF

SELECT tgb
LOCATE FOR czbm=m.czbm and BETWEEN(VAL(LEFT(tcTgsj,4))-VAL(LEFT(tcRzsj,4))+1,zdrznx,zgrznx) &&行定位
IF !FOUND("tgb")
    MESSAGEBOX('套改职务任职时间有误！',64,'系统提示')
    RETURN ""
ENDIF

AFIELDS(lat,"tgb")
FOR i=1 TO ALEN(lat,1)
    IF BETWEEN(VAL(LEFT(tcTgsj,4))-VAL(LEFT(tcCjgzsj,4))+1,VAL(SUBSTR(lat[i,1],2,2)),VAL(SUBSTR(lat[i,1],4,2)))
        EXIT
    ENDIF
ENDFOR

fldname1=lat[i,1]
m.tgdc = tgb. &fldname1
RELEASE lat

RETURN ALLTRIM(m.tgdc)