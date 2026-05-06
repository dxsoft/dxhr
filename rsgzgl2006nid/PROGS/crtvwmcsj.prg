FUNCTION crtvwmcsj

PARAMETERS ttUpdate,tcConn

IF USED("wmcsj")
    USE IN wmcsj
ENDIF
SQLEXEC(tcConn,"select * from wmcsj","wmcsj")

IF ttUpdate
	CURSORSETPROP("tables","wmcsj","wmcsj")
	CURSORSETPROP("keyfieldlist","id","wmcsj")

	fn=""
	FOR i=1 TO FCOUNT("wmcsj")
	    fn=fn+","+FIELD(i,"wmcsj")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"wmcsj")

	fn=""
	FOR i=1 TO FCOUNT("wmcsj")
	    fn=fn+","+FIELD(i,"wmcsj")+" wmcsj."+FIELD(i,"wmcsj")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"wmcsj")

	CURSORSETPROP("sendupdates",.t.,"wmcsj")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT wmcsj

