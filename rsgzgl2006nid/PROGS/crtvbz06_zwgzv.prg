FUNCTION crtvbz06_zwgzv

PARAMETERS ttUpdate,tcConn

IF USED("bz06_zwgz")
    USE IN bz06_zwgz
ENDIF
SQLEXEC(tcConn,"select tbnd,zwbm,'                    ' as zwgw,bz from bz06_zwgz order by tbnd,zwbm","bz06_zwgz")

IF ttUpdate
	CURSORSETPROP("tables","bz06_zwgz","bz06_zwgz")
	CURSORSETPROP("keyfieldlist","tbnd,zwbm","bz06_zwgz")

	fn=""
	FOR i=1 TO FCOUNT("bz06_zwgz")
	    fn=fn+","+FIELD(i,"bz06_zwgz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_zwgz")

	CURSORSETPROP("updatenamelist","tbnd bz06_zwgz.tbnd,zwbm bz06_zwgz.zwbm,bz bz06_zwgz.bz","bz06_zwgz")

	CURSORSETPROP("sendupdates",.t.,"bz06_zwgz")
	CURSORSETPROP("wheretype",2)
ENDIF
