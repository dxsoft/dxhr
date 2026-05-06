FUNCTION crtvmbkhj

PARAMETERS ttUpdate,tcConn

IF USED("mbkhj")
    USE IN mbkhj
ENDIF
SQLEXEC(tcConn,"select * from mbkhj","mbkhj")

IF ttUpdate
	CURSORSETPROP("tables","mbkhj","mbkhj")
	CURSORSETPROP("keyfieldlist","id","mbkhj")

	fn=""
	FOR i=1 TO FCOUNT("mbkhj")
	    fn=fn+","+FIELD(i,"mbkhj")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"mbkhj")

	fn=""
	FOR i=1 TO FCOUNT("mbkhj")
	    fn=fn+","+FIELD(i,"mbkhj")+" mbkhj."+FIELD(i,"mbkhj")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"mbkhj")

	CURSORSETPROP("sendupdates",.t.,"mbkhj")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT mbkhj

