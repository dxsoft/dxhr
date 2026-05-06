FUNCTION crtvbz06_jbt

PARAMETERS ttUpdate,tcConn

IF USED("bz06_jbt")
    USE IN bz06_jbt
ENDIF
SQLEXEC(tcConn,"select tbnd,item,zwbm,mc,worklower,workupper,bz,jxlb from bz06_jbt order by zwbm","bz06_jbt")

IF ttUpdate
	CURSORSETPROP("tables","bz06_jbt","bz06_jbt")
	CURSORSETPROP("keyfieldlist","tbnd,item,zwbm,worklower,workupper,jxlb","bz06_jbt")

	fn=""
	FOR i=1 TO FCOUNT("bz06_jbt")
	    fn=fn+","+FIELD(i,"bz06_jbt")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_jbt")

	fn=""
	FOR i=1 TO FCOUNT("bz06_jbt")
	    fn=fn+","+FIELD(i,"bz06_jbt")+" bz06_jbt."+FIELD(i,"bz06_jbt")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz06_jbt")

	CURSORSETPROP("sendupdates",.t.,"bz06_jbt")
	CURSORSETPROP("wheretype",2)
ENDIF
