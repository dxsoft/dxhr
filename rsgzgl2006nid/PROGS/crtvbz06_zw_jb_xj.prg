FUNCTION crtvbz06_zw_jb_xj

PARAMETERS ttUpdate,tcConn

IF USED("bz06_zw_jb_xj")
    USE IN bz06_zw_jb_xj
ENDIF
SQLEXEC(tcConn,"select id,zwbm,max,min from bz06_zw_jb_xj","bz06_zw_jb_xj")

IF ttUpdate
	CURSORSETPROP("tables","bz06_zw_jb_xj","bz06_zw_jb_xj")
	CURSORSETPROP("keyfieldlist","zwbm","bz06_zw_jb_xj")

	fn=""
	FOR i=1 TO FCOUNT("bz06_zw_jb_xj")
	    fn=fn+","+FIELD(i,"bz06_zw_jb_xj")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_zw_jb_xj")

	fn=""
	FOR i=1 TO FCOUNT("bz06_zw_jb_xj")
	    fn=fn+","+FIELD(i,"bz06_zw_jb_xj")+" bz06_zw_jb_xj."+FIELD(i,"bz06_zw_jb_xj")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz06_zw_jb_xj")

	CURSORSETPROP("sendupdates",.t.,"bz06_zw_jb_xj")
	CURSORSETPROP("wheretype",2)
ENDIF
