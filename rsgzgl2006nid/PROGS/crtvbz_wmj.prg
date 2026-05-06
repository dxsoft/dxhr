FUNCTION crtvbz_wmj

PARAMETERS ttUpdate,tcConn

IF USED("bz_wmj")
    USE IN bz_wmj
ENDIF
SQLEXEC(tcConn,"select * from bz_wmj","bz_wmj")

IF ttUpdate
	CURSORSETPROP("tables","bz_wmj","bz_wmj")
	CURSORSETPROP("keyfieldlist","jb","bz_wmj")

	fn=""
	FOR i=1 TO FCOUNT("bz_wmj")
	    fn=fn+","+FIELD(i,"bz_wmj")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz_wmj")

	fn=""
	FOR i=1 TO FCOUNT("bz_wmj")
	    fn=fn+","+FIELD(i,"bz_wmj")+" bz_wmj."+FIELD(i,"bz_wmj")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz_wmj")

	CURSORSETPROP("sendupdates",.t.,"bz_wmj")
	CURSORSETPROP("wheretype",2)
ENDIF
