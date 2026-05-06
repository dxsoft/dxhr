FUNCTION crtvbz06_fjtgb

PARAMETERS ttUpdate,tcConn

IF USED("bz06_fjtgb")
    USE IN bz06_fjtgb
ENDIF
SQLEXEC(tcConn,"select jb8,jb9,jb10,jb11,jb12,jb13,jb14,jb15,jb16,jb17,jb18,jb19,jb20,jb21,jb22,jb23,jb24,jb25,jb26,d035,d036,d037,d038,d039,d03a,d03b,d03c,d03d from bz06_fjtgb","bz06_fjtgb")

IF ttUpdate
	CURSORSETPROP("tables","bz06_fjtgb","bz06_fjtgb")
	CURSORSETPROP("keyfieldlist","jb8","bz06_fjtgb")

	fn=""
	FOR i=1 TO FCOUNT("bz06_fjtgb")
	    fn=fn+","+FIELD(i,"bz06_fjtgb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_fjtgb")

	fn=""
	FOR i=1 TO FCOUNT("bz06_fjtgb")
	    fn=fn+","+FIELD(i,"bz06_fjtgb")+" bz06_fjtgb."+FIELD(i,"bz06_fjtgb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz06_fjtgb")

	CURSORSETPROP("sendupdates",.t.,"bz06_fjtgb")
	CURSORSETPROP("wheretype",2)
ENDIF
