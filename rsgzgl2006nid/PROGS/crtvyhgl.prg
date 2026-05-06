FUNCTION crtvyhgl

PARAMETERS ttUpdates,tcConn

IF USED("yhgl")
    USE IN yhgl
ENDIF

SQLEXEC(tcConn,"select * from yhgl","yhgl")

IF ttUpdates
	CURSORSETPROP("tables","yhgl","yhgl")
	CURSORSETPROP("keyfieldlist","id","yhgl")

	fn=""
	FOR i=1 TO FCOUNT("yhgl")
        fn=fn+","+FIELD(i,"yhgl")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"yhgl")

	fn=""
	FOR i=1 TO FCOUNT("yhgl")
        fn=fn+","+FIELD(i,"yhgl")+" yhgl."+FIELD(i,"yhgl")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"yhgl")

	CURSORSETPROP("sendupdates",.t.,"yhgl")
	CURSORSETPROP("wheretype",2)
ENDIF