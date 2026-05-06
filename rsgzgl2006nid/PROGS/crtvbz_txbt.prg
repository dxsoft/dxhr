FUNCTION crtvbz_txbt

PARAMETERS ttUpdate,tcConn

IF USED("bz_txbt")
    USE IN bz_txbt
ENDIF
SQLEXEC(tcConn,"select * from bz_txbt","bz_txbt")

IF ttUpdate
	CURSORSETPROP("tables","bz_txbt","bz_txbt")
	CURSORSETPROP("keyfieldlist","tbnd,zwbm","bz_txbt")

	fn=""
	FOR i=1 TO FCOUNT("bz_txbt")
	    fn=fn+","+FIELD(i,"bz_txbt")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz_txbt")

	fn=""
	FOR i=1 TO FCOUNT("bz_txbt")
	    fn=fn+","+FIELD(i,"bz_txbt")+" bz_txbt."+FIELD(i,"bz_txbt")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz_txbt")

	CURSORSETPROP("sendupdates",.t.,"bz_txbt")
	CURSORSETPROP("wheretype",2)
ENDIF
