FUNCTION crtvbz_jcjxj

PARAMETERS ttUpdate,tcConn

IF USED("bz_jcjxj")
    USE IN bz_jcjxj
ENDIF
SQLEXEC(tcConn,"select * from bz_jcjxj","bz_jcjxj")

IF ttUpdate
	CURSORSETPROP("tables","bz_jcjxj","bz_jcjxj")
	CURSORSETPROP("keyfieldlist","id","bz_jcjxj")

	fn=""
	FOR i=1 TO FCOUNT("bz_jcjxj")
	    fn=fn+","+FIELD(i,"bz_jcjxj")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz_jcjxj")

	fn=""
	FOR i=1 TO FCOUNT("bz_jcjxj")
	    fn=fn+","+FIELD(i,"bz_jcjxj")+" bz_jcjxj."+FIELD(i,"bz_jcjxj")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz_jcjxj")

	CURSORSETPROP("sendupdates",.t.,"bz_jcjxj")
	CURSORSETPROP("wheretype",2)
ENDIF