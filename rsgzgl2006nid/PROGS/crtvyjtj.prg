FUNCTION crtvyjtj

PARAMETERS ttUpdate,tcConn

IF USED("yjtj")
    USE IN yjtj
ENDIF
SQLEXEC(tcConn,"select * from yjtj","yjtj")

IF ttUpdate
	CURSORSETPROP("tables","yjtj","yjtj")
	CURSORSETPROP("keyfieldlist","yjsj","yjtj")

	fn=""
	FOR i=1 TO FCOUNT("yjtj")
	    fn=fn+","+FIELD(i,"yjtj")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"yjtj")

	fn=""
	FOR i=1 TO FCOUNT("yjtj")
	    fn=fn+","+FIELD(i,"yjtj")+" yjtj."+FIELD(i,"yjtj")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"yjtj")

	CURSORSETPROP("sendupdates",.t.,"yjtj")
	CURSORSETPROP("wheretype",2)
ENDIF
