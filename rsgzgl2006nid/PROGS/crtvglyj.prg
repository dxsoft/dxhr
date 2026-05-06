FUNCTION crtvglyj

PARAMETERS ttUpdate,tcConn

IF USED("glyj")
    USE IN glyj
ENDIF
SQLEXEC(tcConn,"select * from glyj","glyj")

IF ttUpdate
	CURSORSETPROP("tables","glyj","glyj")
	CURSORSETPROP("keyfieldlist","yjsj","glyj")

	fn=""
	FOR i=1 TO FCOUNT("glyj")
	    fn=fn+","+FIELD(i,"glyj")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"glyj")

	fn=""
	FOR i=1 TO FCOUNT("glyj")
	    fn=fn+","+FIELD(i,"glyj")+" glyj."+FIELD(i,"glyj")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"glyj")

	CURSORSETPROP("sendupdates",.t.,"glyj")
	CURSORSETPROP("wheretype",2)
ENDIF
