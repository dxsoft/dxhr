FUNCTION crtvbz06_zwgz_fjv

PARAMETERS ttUpdate,tcConn

IF USED("bz06_zwgz_fj")
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

csql="tbnd bz06_zwgz_fj.tbnd,zwbm bz06_zwgz_fj.zwbm,zwmc bz06_zwgz_fj.zwmc,dc1 bz06_zwgz_fj.dc1,dc2 bz06_zwgz_fj.dc2,dc3 bz06_zwgz_fj.dc3,dc4 bz06_zwgz_fj.dc4,dc5 bz06_zwgz_fj.dc5,dc6 bz06_zwgz_fj.dc6,"
csql=csql+"dc7 bz06_zwgz_fj.dc7,dc8 bz06_zwgz_fj.dc8,dc9 bz06_zwgz_fj.dc9,dc10 bz06_zwgz_fj.dc10,dc11 bz06_zwgz_fj.dc11,dc12 bz06_zwgz_fj.dc12,dc13 bz06_zwgz_fj.dc13,dc14 bz06_zwgz_fj.dc14,dc15 bz06_zwgz_fj.dc15,"
csql=csql+"dc16 bz06_zwgz_fj.dc16,dc17 bz06_zwgz_fj.dc17"
	CURSORSETPROP("updatenamelist",csql,"bz06_zwgz_fj")

	CURSORSETPROP("sendupdates",.t.,"bz06_zwgz_fj")
	CURSORSETPROP("wheretype",2)
ENDIF
