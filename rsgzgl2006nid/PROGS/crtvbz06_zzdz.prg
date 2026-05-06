FUNCTION crtvbz06_zzdz

PARAMETERS ttUpdate,tcConn

IF USED("bz06_zzdz")
    USE IN bz06_zzdz
ENDIF
SQLEXEC(tcConn,"select tbnd,xlbm,xlmc,zzzwbm,zzzwmc,zzdc,zzjb,gz1,gz2 from bz06_zzdz","bz06_zzdz")

IF ttUpdate
	CURSORSETPROP("tables","bz06_zzdz","bz06_zzdz")
	CURSORSETPROP("keyfieldlist","tbnd,xlbm,zzzwbm","bz06_zzdz")

	fn=""
	FOR i=1 TO FCOUNT("bz06_zzdz")
	    fn=fn+","+FIELD(i,"bz06_zzdz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bz06_zzdz")

	fn=""
	FOR i=1 TO FCOUNT("bz06_zzdz")
	    fn=fn+","+FIELD(i,"bz06_zzdz")+" bz06_zzdz."+FIELD(i,"bz06_zzdz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bz06_zzdz")

	CURSORSETPROP("sendupdates",.t.,"bz06_zzdz")
	CURSORSETPROP("wheretype",2)
ENDIF
