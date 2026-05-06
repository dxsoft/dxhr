FUNCTION crtvjdgzbz

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select * from jdgzbz","jdgzbz")

IF ttUpdates
	CURSORSETPROP("tables","jdgzbz","jdgzbz")
	CURSORSETPROP("keyfieldlist","tbnd,zwbm","jdgzbz")

	fn=""
	FOR i=1 TO FCOUNT("jdgzbz")
        fn=fn+","+FIELD(i,"jdgzbz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jdgzbz")

	fn=""
	FOR i=1 TO FCOUNT("jdgzbz")
        fn=fn+","+FIELD(i,"jdgzbz")+" jdgzbz."+FIELD(i,"jdgzbz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jdgzbz")

	CURSORSETPROP("sendupdates",.t.,"jdgzbz")
	CURSORSETPROP("wheretype",2,"jdgzbz")
ENDIF
