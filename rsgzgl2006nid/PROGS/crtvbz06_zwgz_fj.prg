FUNCTION crtvbz06_zwgz_fj

PARAMETERS ttUpdate,tcConn

IF USED("bz06_zwgz_fj")
    USE IN bz06_zwgz_fj
ENDIF
SQLEXEC(tcConn,"select tbnd,zwbm,zwmc,dc1,dc2,dc3,dc4,dc5,dc6,dc7,dc8,dc9,dc10,dc11,dc12,dc13,dc14,dc15,dc16,dc17 from bz06_zwgz_fj","bz06_zwgz_fj")

IF ttUpdate
	CURSORSETPROP("tables","bz06_zwgz_fj","bz06_zwgz_fj")
	CURSORSETPROP("keyfieldlist","tbnd,zwbm","bz06_zwgz_fj")

	fn=""
	FOR i=1 TO FCOUNT("bz06_zwgz_fj")
	    fn=fn+","+FIELD(i,"bz06_zwgz_fj")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_zwgz_fj")

	fn=""
	FOR i=1 TO FCOUNT("bz06_zwgz_fj")
	    fn=fn+","+FIELD(i,"bz06_zwgz_fj")+" bz06_zwgz_fj."+FIELD(i,"bz06_zwgz_fj")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz06_zwgz_fj")

	CURSORSETPROP("sendupdates",.t.,"bz06_zwgz_fj")
	CURSORSETPROP("wheretype",2)
ENDIF
