FUNCTION crtvnxj

PARAMETERS ttUpdate,tcConn

IF USED("nxj")
    USE IN nxj
ENDIF
SQLEXEC(tcConn,"select * from nxj","nxj")

IF ttUpdate
	CURSORSETPROP("tables","nxj","nxj")
	CURSORSETPROP("keyfieldlist","id","nxj")

	fn=""
	FOR i=1 TO FCOUNT("nxj")
	    fn=fn+","+FIELD(i,"nxj")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"nxj")

	fn=""
	FOR i=1 TO FCOUNT("nxj")
	    fn=fn+","+FIELD(i,"nxj")+" nxj."+FIELD(i,"nxj")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"nxj")

	CURSORSETPROP("sendupdates",.t.,"nxj")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT nxj

