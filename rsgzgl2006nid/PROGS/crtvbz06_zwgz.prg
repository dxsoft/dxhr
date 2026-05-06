FUNCTION crtvbz06_zwgz

PARAMETERS ttUpdate,tcConn

IF USED("bz06_zwgz")
    USE IN bz06_zwgz
ENDIF
SQLEXEC(tcConn,"select tbnd,zwbm,bz from bz06_zwgz order by tbnd,zwbm","bz06_zwgz")

IF ttUpdate
	CURSORSETPROP("tables","bz06_zwgz","bz06_zwgz")
	CURSORSETPROP("keyfieldlist","tbnd,zwbm","bz06_zwgz")

	fn=""
	FOR i=1 TO FCOUNT("bz06_zwgz")
	    fn=fn+","+FIELD(i,"bz06_zwgz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_zwgz")

	fn=""
	FOR i=1 TO FCOUNT("bz06_zwgz")
	    fn=fn+","+FIELD(i,"bz06_zwgz")+" bz06_zwgz."+FIELD(i,"bz06_zwgz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz06_zwgz")

	CURSORSETPROP("sendupdates",.t.,"bz06_zwgz")
	CURSORSETPROP("wheretype",2)
ENDIF
