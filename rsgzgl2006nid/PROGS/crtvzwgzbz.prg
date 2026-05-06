FUNCTION crtvzwgzbz

PARAMETERS ttUpdate,tcConn

IF USED("zwgzbz")
    USE IN zwgzbz
ENDIF
SQLEXEC(tcConn,"select * from zwgzbz","zwgzbz")

IF ttUpdate
	CURSORSETPROP("tables","zwgzbz","zwgzbz")
	CURSORSETPROP("keyfieldlist","tbnd,zwbm","zwgzbz")

	fn=""
	FOR i=1 TO FCOUNT("zwgzbz")
	    fn=fn+","+FIELD(i,"zwgzbz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"zwgzbz")

	fn=""
	FOR i=1 TO FCOUNT("zwgzbz")
	    fn=fn+","+FIELD(i,"zwgzbz")+" zwgzbz."+FIELD(i,"zwgzbz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"zwgzbz")

	CURSORSETPROP("sendupdates",.t.,"zwgzbz")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT zwgzbz
INDEX ON tbnd+zwbm TAG ndbm ADDITIVE
INDEX ON tbnd TAG tbnd ADDITIVE
