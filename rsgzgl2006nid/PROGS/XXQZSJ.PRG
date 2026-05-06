FUNCTION xxqzsj

PARAMETERS tcdwgrbm

oldalias=ALIAS()

IF !USED("dxl")
    USE dxl ALIAS dxl IN 0
ENDIF
SELECT dxl
LOCATE FOR dwbm+grbm=tcdwgrbm AND INLIST(ALLTRIM(xl),"博士","硕士","研究生","双学士","本科","大普","专科") AND xllb="普通全日制"
IF !EMPTY(oldalias)
    SELECT (oldalias)
ENDIF

IF FOUND("dxl")
    RETURN dxl.rxsj+"-"+dxl.bysj
ELSE
    RETURN ""
ENDIF
