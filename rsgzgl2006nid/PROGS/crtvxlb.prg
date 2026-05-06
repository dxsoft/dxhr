FUNCTION crtvxlb

PARAMETERS ttUpdate,tcConn

IF USED("xlb")
    USE IN xlb
ENDIF

SQLEXEC(tcConn,"select * from dxlb where dwbm='"+ALLTRIM(m.pdwbm)+"' order by bysj","xlb")
CURSORSETPROP("tables","dxlb","xlb")
CURSORSETPROP("keyfieldlist","id","xlb")

fn=""
FOR i=1 TO FCOUNT("xlb")
    IF FIELD(i,"xlb")<>"ID"
        fn=fn+","+FIELD(i,"xlb")
    ENDIF
ENDFOR

CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"xlb")

fn=""
FOR i=1 TO FCOUNT("xlb")
    fn=fn+","+FIELD(i,"xlb")+" dxlb."+FIELD(i,"xlb")
ENDFOR

CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"xlb")

CURSORSETPROP("sendupdates",.t.,"xlb")
CURSORSETPROP("wheretype",2,"xlb")

SELECT xlb
INDEX ON dwbm+grbm+xl TAG bmxl ADDITIVE