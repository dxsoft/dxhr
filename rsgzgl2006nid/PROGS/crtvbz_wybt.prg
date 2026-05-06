FUNCTION crtvbz_wybt

PARAMETERS ttUpdate,tcConn

IF USED("bz_wybt")
    USE IN bz_wybt
ENDIF
SQLEXEC(tcConn,"select * from bz_wybt","bz_wybt")

IF ttUpdate
	CURSORSETPROP("tables","bz_wybt","bz_wybt")
	CURSORSETPROP("keyfieldlist","tbnd,zwbm","bz_wybt")

	fn=""
	FOR i=1 TO FCOUNT("bz_wybt")
	    fn=fn+","+FIELD(i,"bz_wybt")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz_wybt")

	fn=""
	FOR i=1 TO FCOUNT("bz_wybt")
	    fn=fn+","+FIELD(i,"bz_wybt")+" bz_wybt."+FIELD(i,"bz_wybt")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz_wybt")

	CURSORSETPROP("sendupdates",.t.,"bz_wybt")
	CURSORSETPROP("wheretype",2)
ENDIF
