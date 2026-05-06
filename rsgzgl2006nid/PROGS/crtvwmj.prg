FUNCTION crtvwmj

PARAMETERS ttUpdate,tcConn

IF USED("wmj")
    USE IN wmj
ENDIF
SQLEXEC(tcConn,"select * from wmj","wmj")

IF ttUpdate
	CURSORSETPROP("tables","wmj","wmj")
	CURSORSETPROP("keyfieldlist","id","wmj")

	fn=""
	FOR i=1 TO FCOUNT("wmj")
	    fn=fn+","+FIELD(i,"wmj")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"wmj")

	fn=""
	FOR i=1 TO FCOUNT("wmj")
	    fn=fn+","+FIELD(i,"wmj")+" wmj."+FIELD(i,"wmj")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"wmj")

	CURSORSETPROP("sendupdates",.t.,"wmj")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT wmj

