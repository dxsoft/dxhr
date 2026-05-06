FUNCTION crtvbdry

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select * from bdry where dwbm='"+m.pdwbm+"'","bdry")

IF ttUpdates
	CURSORSETPROP("tables","bdry","bdry")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,bz","bdry")

	fn=""
	FOR i=1 TO FCOUNT("bdry")
        fn=fn+","+FIELD(i,"bdry")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"bdry")

	fn=""
	FOR i=1 TO FCOUNT("bdry")
        fn=fn+","+FIELD(i,"bdry")+" bdry."+FIELD(i,"bdry")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"bdry")

	CURSORSETPROP("sendupdates",.t.,"bdry")
	CURSORSETPROP("wheretype",2)
ENDIF
