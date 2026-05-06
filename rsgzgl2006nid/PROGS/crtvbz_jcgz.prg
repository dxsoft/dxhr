FUNCTION crtvbz_jcgz

PARAMETERS ttUpdate,tcConn

IF USED("bz_jcgz")
    USE IN bz_jcgz
ENDIF
SQLEXEC(tcConn,"select * from bz_jcgz order by xlbm","bz_jcgz")

IF ttUpdate
	CURSORSETPROP("tables","bz_jcgz","bz_jcgz")
	CURSORSETPROP("keyfieldlist","id","bz_jcgz")

	fn=""
	FOR i=1 TO FCOUNT("bz_jcgz")
	    fn=fn+","+FIELD(i,"bz_jcgz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz_jcgz")

	fn=""
	FOR i=1 TO FCOUNT("bz_jcgz")
	    fn=fn+","+FIELD(i,"bz_jcgz")+" bz_jcgz."+FIELD(i,"bz_jcgz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz_jcgz")

	CURSORSETPROP("sendupdates",.t.,"bz_jcgz")
	CURSORSETPROP("wheretype",2)
ENDIF
