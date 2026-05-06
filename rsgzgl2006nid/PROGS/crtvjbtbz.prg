FUNCTION crtvjbtbz

PARAMETERS ttUpdate,tcConn

IF USED("jbtbz")
    USE IN jbtbz
ENDIF
SQLEXEC(tcConn,"select * from jbtbz","jbtbz")

IF ttUpdate
	CURSORSETPROP("tables","jbtbz","jbtbz")
	CURSORSETPROP("keyfieldlist","tbnd,bm","jbtbz")

	fn=""
	FOR i=1 TO FCOUNT("jbtbz")
	    fn=fn+","+FIELD(i,"jbtbz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jbtbz")

	fn=""
	FOR i=1 TO FCOUNT("jbtbz")
	    fn=fn+","+FIELD(i,"jbtbz")+" jbtbz."+FIELD(i,"jbtbz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jbtbz")

	CURSORSETPROP("sendupdates",.t.,"jbtbz")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT jbtbz
INDEX ON tbnd+bm TAG ndbm ADDITIVE
INDEX ON tbnd TAG tbnd ADDITIVE
