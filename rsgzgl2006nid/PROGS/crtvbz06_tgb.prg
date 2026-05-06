FUNCTION crtvbz06_tgb

PARAMETERS ttUpdate,tcConn

IF USED("bz06_tgb")
    USE IN bz06_tgb
ENDIF
SQLEXEC(tcConn,"select zwbm,rzns,rznz,tgns,tgnz,jb,dc from bz06_tgb","bz06_tgb")

IF ttUpdate
	CURSORSETPROP("tables","bz06_tgb","bz06_tgb")
	CURSORSETPROP("keyfieldlist","zwbm,rzns,rznz,tgns,tgnz","bz06_tgb")

	fn=""
	FOR i=1 TO FCOUNT("bz06_tgb")
	    fn=fn+","+FIELD(i,"bz06_tgb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_tgb")

	fn=""
	FOR i=1 TO FCOUNT("bz06_tgb")
	    fn=fn+","+FIELD(i,"bz06_tgb")+" bz06_tgb."+FIELD(i,"bz06_tgb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz06_tgb")

	CURSORSETPROP("sendupdates",.t.,"bz06_tgb")
	CURSORSETPROP("wheretype",2)
ENDIF
