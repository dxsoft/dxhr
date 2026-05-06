FUNCTION crtvbz06_djgz

PARAMETERS ttUpdate,tcConn

IF USED("bz06_djgz")
    USE IN bz06_djgz
ENDIF
SQLEXEC(tcConn,"select tbnd,jb,dc1,dc2,dc3,dc4,dc5,dc6,dc7,dc8,dc9,dc10,dc11,dc12,dc13,dc14 from bz06_djgz","bz06_djgz")

IF ttUpdate
	CURSORSETPROP("tables","bz06_djgz","bz06_djgz")
	CURSORSETPROP("keyfieldlist","tbnd,jb","bz06_djgz")

	fn=""
	FOR i=1 TO FCOUNT("bz06_djgz")
	    fn=fn+","+FIELD(i,"bz06_djgz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_djgz")

	fn=""
	FOR i=1 TO FCOUNT("bz06_djgz")
	    fn=fn+","+FIELD(i,"bz06_djgz")+" bz06_djgz."+FIELD(i,"bz06_djgz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz06_djgz")

	CURSORSETPROP("sendupdates",.t.,"bz06_djgz")
	CURSORSETPROP("wheretype",2)
ENDIF
