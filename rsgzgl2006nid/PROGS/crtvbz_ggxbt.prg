FUNCTION crtvbz_ggxbt

PARAMETERS ttUpdate,tcConn

IF USED("bz_ggxbt")
    USE IN bz_ggxbt
ENDIF
SQLEXEC(tcConn,"select * from bz_ggxbt","bz_ggxbt")

IF ttUpdate
	CURSORSETPROP("tables","bz_ggxbt","bz_ggxbt")
	CURSORSETPROP("keyfieldlist","id","bz_ggxbt")

	fn=""
	FOR i=1 TO FCOUNT("bz_ggxbt")
	    fn=fn+","+FIELD(i,"bz_ggxbt")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz_ggxbt")

	fn=""
	FOR i=1 TO FCOUNT("bz_ggxbt")
	    fn=fn+","+FIELD(i,"bz_ggxbt")+" bz_wybt."+FIELD(i,"bz_ggxbt")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz_ggxbt")

	CURSORSETPROP("sendupdates",.t.,"bz_ggxbt")
	CURSORSETPROP("wheretype",2)
ENDIF