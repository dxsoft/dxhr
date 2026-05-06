FUNCTION crtvbz_gwjt

PARAMETERS ttUpdate,tcConn

IF USED("bz_gwjt")
    USE IN bz_gwjt
ENDIF
SQLEXEC(tcConn,"select * from bz_gwjt","bz_gwjt")

IF ttUpdate
	CURSORSETPROP("tables","bz_gwjt","bz_gwjt")
	CURSORSETPROP("keyfieldlist","tbnd,lb","bz_gwjt")

	fn=""
	FOR i=1 TO FCOUNT("bz_gwjt")
	    fn=fn+","+FIELD(i,"bz_gwjt")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz_gwjt")

	fn=""
	FOR i=1 TO FCOUNT("bz_gwjt")
	    fn=fn+","+FIELD(i,"bz_gwjt")+" bz_gwjt."+FIELD(i,"bz_gwjt")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz_gwjt")

	CURSORSETPROP("sendupdates",.t.,"bz_gwjt")
	CURSORSETPROP("wheretype",2)
ENDIF
