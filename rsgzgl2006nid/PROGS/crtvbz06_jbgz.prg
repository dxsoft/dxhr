FUNCTION crtvbz06_jbgz

PARAMETERS ttUpdate,tcConn

IF USED("bz06_jbgz")
    USE IN bz06_jbgz
ENDIF
SQLEXEC(tcConn,"select tbnd,jb,dc1,dc2,dc3,dc4,dc5,dc6,dc7,dc8,dc9,dc10,dc11,dc12,dc13,dc14,dc15,dc16,dc17,dc18,dc19,dc20 from bz06_jbgz","bz06_jbgz")

IF ttUpdate
	CURSORSETPROP("tables","bz06_jbgz","bz06_jbgz")
	CURSORSETPROP("keyfieldlist","tbnd,jb","bz06_jbgz")

	fn=""
	FOR i=1 TO FCOUNT("bz06_jbgz")
	    fn=fn+","+FIELD(i,"bz06_jbgz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_jbgz")

	fn=""
	FOR i=1 TO FCOUNT("bz06_jbgz")
	    fn=fn+","+FIELD(i,"bz06_jbgz")+" bz06_jbgz."+FIELD(i,"bz06_jbgz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz06_jbgz")

	CURSORSETPROP("sendupdates",.t.,"bz06_jbgz")
	CURSORSETPROP("wheretype",2)
ENDIF
