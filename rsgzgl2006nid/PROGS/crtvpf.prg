FUNCTION crtvpf

PARAMETERS ttUpdates,tcConn

IF USED("pf")
    USE IN pf
ENDIF

SQLEXEC(tcConn,"select * from pf","pf")

IF ttUpdates
	CURSORSETPROP("tables","pf","pf")
	CURSORSETPROP("keyfieldlist","spwj","pf")

	fn=""
	FOR i=1 TO FCOUNT("pf")
        fn=fn+","+FIELD(i,"pf")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"pf")

	fn=""
	FOR i=1 TO FCOUNT("pf")
        fn=fn+","+FIELD(i,"pf")+" pf."+FIELD(i,"pf")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"pf")

	CURSORSETPROP("sendupdates",.t.,"pf")
	CURSORSETPROP("wheretype",2)
ENDIF
