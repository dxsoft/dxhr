FUNCTION crtvcwjl

PARAMETERS ttUpdate,tcConn

SQLEXEC(tcConn,"select * from cwjl order by 时间","cwjl")
CURSORSETPROP("tables","cwjl","cwjl")
CURSORSETPROP("keyfieldlist","时间","cwjl")

fn=""
FOR i=1 TO FCOUNT("cwjl")
    fn=fn+","+FIELD(i,"cwjl")
ENDFOR

CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"cwjl")

fn=""
FOR i=1 TO FCOUNT("cwjl")
    fn=fn+","+FIELD(i,"cwjl")+" cwjl."+FIELD(i,"cwjl")
ENDFOR

CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"cwjl")

CURSORSETPROP("sendupdates",.t.,"cwjl")
CURSORSETPROP("wheretype",2,"cwjl")
