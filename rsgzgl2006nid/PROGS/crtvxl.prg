FUNCTION crtvxl

PARAMETERS ttUpdate,tcConn

IF USED("xl")
    USE IN xl
ENDIF

SQLEXEC(tcConn,"select * from dxl where dwbm='"+ALLTRIM(m.pdwbm)+"'","xl")
CURSORSETPROP("tables","dxl","xl")

*!*	CURSORSETPROP("keyfieldlist","dwbm,grbm,xlbm,bysj","xl")

CURSORSETPROP("keyfieldlist","id","xl")


fn=""
FOR i=1 TO FCOUNT("xl")
    IF FIELD(i,"xl")<>"ID"
        fn=fn+","+FIELD(i,"xl")
    ENDIF
ENDFOR

CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"xl")

fn=""
FOR i=1 TO FCOUNT("xl")
    fn=fn+","+FIELD(i,"xl")+" dxl."+FIELD(i,"xl")
ENDFOR

CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"xl")

CURSORSETPROP("sendupdates",.t.,"xl")
CURSORSETPROP("wheretype",2,"xl")

SELECT xl
INDEX ON dwbm+grbm+xl TAG bmxl ADDITIVE
INDEX ON dwbm+grbm+xl+bysj TAG bmxlny ADDITIVE
INDEX ON dwbm+grbm+bysj TAG bmny ADDITIVE
