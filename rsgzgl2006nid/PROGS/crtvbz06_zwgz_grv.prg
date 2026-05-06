FUNCTION crtvbz06_zwgz_grv

PARAMETERS ttUpdate,tcConn

IF USED("bz06_zwgz_gr")
    USE IN bz06_zwgz_gr
ENDIF
SQLEXEC(tcConn,"select tbnd,zwbm,'                    ' as zwgw,dc1,dc2,dc3,dc4,dc5,dc6,dc7,dc8,dc9,dc10,dc11,dc12,dc13,dc14,dc15,dc16,dc17,dc18,dc19,dc20,jsdjgz from bz06_zwgz_gr","bz06_zwgz_gr")

IF ttUpdate
	CURSORSETPROP("tables","bz06_zwgz_gr","bz06_zwgz_gr")
	CURSORSETPROP("keyfieldlist","tbnd,zwbm","bz06_zwgz_gr")

	fn=""
	FOR i=1 TO FCOUNT("bz06_zwgz_gr")
	    fn=fn+","+FIELD(i,"bz06_zwgz_gr")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_zwgz_gr")

csql="tbnd bz06_zwgz_gr.tbnd,zwbm bz06_zwgz_gr.zwbm,dc1 bz06_zwgz_gr.dc1,dc2 bz06_zwgz_gr.dc2,dc3 bz06_zwgz_gr.dc3,dc4 bz06_zwgz_gr.dc4,dc5 bz06_zwgz_gr.dc5,dc6 bz06_zwgz_gr.dc6,"
csql=csql+"dc7 bz06_zwgz_gr.dc7,dc8 bz06_zwgz_gr.dc8,dc9 bz06_zwgz_gr.dc9,dc10 bz06_zwgz_gr.dc10,dc11 bz06_zwgz_gr.dc11,dc12 bz06_zwgz_gr.dc12,dc13 bz06_zwgz_gr.dc13,dc14 bz06_zwgz_gr.dc14,dc15 bz06_zwgz_gr.dc15,"
csql=csql+"dc16 bz06_zwgz_gr.dc16,dc17 bz06_zwgz_gr.dc17,dc18 bz06_zwgz_gr.dc18,dc19 bz06_zwgz_gr.dc19,dc20 bz06_zwgz_gr.dc20,jsdjgz bz06_zwgz_gr.jsdjgz"
	CURSORSETPROP("updatenamelist",csql,"bz06_zwgz_gr")

	CURSORSETPROP("sendupdates",.t.,"bz06_zwgz_gr")
	CURSORSETPROP("wheretype",2)
ENDIF
