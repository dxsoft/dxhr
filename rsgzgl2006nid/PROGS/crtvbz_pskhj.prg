FUNCTION crtvbz_pskhj

PARAMETERS ttUpdate,tcConn

IF USED("bz_pskhj")
    USE IN bz_pskhj
ENDIF
SQLEXEC(tcConn,"select * from bz_pskhj","bz_pskhj")

IF ttUpdate
	CURSORSETPROP("tables","bz_pskhj","bz_pskhj")
	CURSORSETPROP("keyfieldlist","tbnd,khjg","bz_pskhj")

	fn=""
	FOR i=1 TO FCOUNT("bz_pskhj")
	    fn=fn+","+FIELD(i,"bz_pskhj")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz_pskhj")

	fn=""
	FOR i=1 TO FCOUNT("bz_pskhj")
	    fn=fn+","+FIELD(i,"bz_pskhj")+" bz_gwjt."+FIELD(i,"bz_pskhj")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz_pskhj")

	CURSORSETPROP("sendupdates",.t.,"bz_pskhj")
	CURSORSETPROP("wheretype",2)
ENDIF
