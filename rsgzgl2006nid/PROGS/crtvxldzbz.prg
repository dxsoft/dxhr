FUNCTION crtvxldzbz

PARAMETERS ttUpdate,tcConn

SQLEXEC(tcConn,"select * from xldzbz","xldzbz")
CURSORSETPROP("tables","xldzbz","xldzbz")
*!*	CURSORSETPROP("keyfieldlist","dwbm,grbm,xlbm,bysj","xl")

*!*	fn=""
*!*	FOR i=1 TO FCOUNT("xl")
*!*	    fn=fn+","+FIELD(i,"xl")
*!*	ENDFOR

*!*	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"xl")

*!*	fn=""
*!*	FOR i=1 TO FCOUNT("xl")
*!*	    fn=fn+","+FIELD(i,"xl")+" dxl."+FIELD(i,"xl")
*!*	ENDFOR

*!*	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"xl")

*!*	CURSORSETPROP("sendupdates",.t.,"xl")
*!*	CURSORSETPROP("wheretype",2,"xl")
