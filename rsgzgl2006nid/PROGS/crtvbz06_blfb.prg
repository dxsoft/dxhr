FUNCTION crtvbz06_blfb

PARAMETERS ttUpdate,tcConn

IF USED("bz06_blfb")
    USE IN bz06_blfb
ENDIF
SQLEXEC(tcConn,"select zwbm,mc,bz from bz06_blfb","bz06_blfb")

IF ttUpdate
	CURSORSETPROP("tables","bz06_blfb","bz06_blfb")
	CURSORSETPROP("keyfieldlist","zwbm","bz06_blfb")

	fn=""
	FOR i=1 TO FCOUNT("bz06_blfb")
	    fn=fn+","+FIELD(i,"bz06_blfb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_blfb")

	fn=""
	FOR i=1 TO FCOUNT("bz06_blfb")
	    fn=fn+","+FIELD(i,"bz06_blfb")+" bz06_blfb."+FIELD(i,"bz06_blfb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz06_blfb")

	CURSORSETPROP("sendupdates",.t.,"bz06_blfb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT bz06_blfb
INDEX ON zwbm TAG zwbm

