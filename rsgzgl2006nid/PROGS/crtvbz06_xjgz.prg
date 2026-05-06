FUNCTION crtvbz06_xjgz

PARAMETERS ttUpdate,tcConn

IF USED("bz06_xjgz")
    USE IN bz06_xjgz
ENDIF
SQLEXEC(tcConn,"select * from bz06_xjgz","bz06_xjgz")

IF ttUpdate
	CURSORSETPROP("tables","bz06_xjgz","bz06_xjgz")
	CURSORSETPROP("keyfieldlist","tbnd,gwflbm,xj","bz06_xjgz")

	fn=""
	FOR i=1 TO FCOUNT("bz06_xjgz")
	    fn=fn+","+FIELD(i,"bz06_xjgz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_xjgz")

	fn=""
	FOR i=1 TO FCOUNT("bz06_xjgz")
	    fn=fn+","+FIELD(i,"bz06_xjgz")+" bz06_xjgz."+FIELD(i,"bz06_xjgz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz06_xjgz")

	CURSORSETPROP("sendupdates",.t.,"bz06_xjgz")
	CURSORSETPROP("wheretype",2)
ENDIF
