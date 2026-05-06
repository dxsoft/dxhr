FUNCTION crtvbz06_jjjy

PARAMETERS ttUpdate,tcConn

IF USED("bz06_jjjy")
    USE IN bz06_jjjy
ENDIF
SQLEXEC(tcConn,"select zwbm,zwmc,a1,a2,a3,a4,a5 from bz06_jjjy","bz06_jjjy")

IF ttUpdate
	CURSORSETPROP("tables","bz06_jjjy","bz06_jjjy")
	CURSORSETPROP("keyfieldlist","zwbm","bz06_jjjy")

	fn=""
	FOR i=1 TO FCOUNT("bz06_jjjy")
	    fn=fn+","+FIELD(i,"bz06_jjjy")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_jjjy")

	fn=""
	FOR i=1 TO FCOUNT("bz06_jjjy")
	    fn=fn+","+FIELD(i,"bz06_jjjy")+" bz06_jjjy."+FIELD(i,"bz06_jjjy")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz06_jjjy")

	CURSORSETPROP("sendupdates",.t.,"bz06_jjjy")
	CURSORSETPROP("wheretype",2)
ENDIF
