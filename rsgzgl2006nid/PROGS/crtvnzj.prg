FUNCTION crtvnzj

PARAMETERS ttUpdate,tcConn

IF USED("nzj")
    USE IN nzj
ENDIF
SQLEXEC(tcConn,"select * from nzj","nzj")

IF ttUpdate
	CURSORSETPROP("tables","nzj","nzj")
	CURSORSETPROP("keyfieldlist","ID","nzj")

	fn=""
	FOR i=1 TO FCOUNT("nzj")
	    fn=fn+","+FIELD(i,"nzj")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"nzj")

	fn=""
	FOR i=1 TO FCOUNT("nzj")
	    fn=fn+","+FIELD(i,"nzj")+" nzj."+FIELD(i,"nzj")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"nzj")

	CURSORSETPROP("sendupdates",.t.,"nzj")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT nzj
