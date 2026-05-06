FUNCTION crtvbz06_zwgz_gr

PARAMETERS ttUpdate,tcConn

IF USED("bz06_zwgz_gr")
    USE IN bz06_zwgz_gr
ENDIF
SQLEXEC(tcConn,"select tbnd,zwbm,dc1,dc2,dc3,dc4,dc5,dc6,dc7,dc8,dc9,dc10,dc11,dc12,dc13,dc14,dc15,dc16,dc17,dc18,dc19,dc20,jsdjgz from bz06_zwgz_gr","bz06_zwgz_gr")

IF ttUpdate
	CURSORSETPROP("tables","bz06_zwgz_gr","bz06_zwgz_gr")
	CURSORSETPROP("keyfieldlist","tbnd,zwbm","bz06_zwgz_gr")

	fn=""
	FOR i=1 TO FCOUNT("bz06_zwgz_gr")
	    fn=fn+","+FIELD(i,"bz06_zwgz_gr")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_zwgz_gr")

	fn=""
	FOR i=1 TO FCOUNT("bz06_zwgz_gr")
	    fn=fn+","+FIELD(i,"bz06_zwgz_gr")+" bz06_zwgz_gr."+FIELD(i,"bz06_zwgz_gr")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz06_zwgz_gr")

	CURSORSETPROP("sendupdates",.t.,"bz06_zwgz_gr")
	CURSORSETPROP("wheretype",2)
ENDIF
