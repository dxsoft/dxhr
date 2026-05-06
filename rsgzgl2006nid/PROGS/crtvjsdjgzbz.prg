FUNCTION crtvjsdjgzbz

PARAMETERS ttUpdate,tcConn

IF USED("jsdjgzbz")
    USE IN jsdjgzbz
ENDIF
SQLEXEC(tcConn,"select * from jsdjgzbz","jsdjgzbz")

IF ttUpdate
	CURSORSETPROP("tables","jsdjgzbz","jsdjgzbz")
	CURSORSETPROP("keyfieldlist","tbnd,zwbm","jsdjgzbz")

	fn=""
	FOR i=1 TO FCOUNT("jsdjgzbz")
	    fn=fn+","+FIELD(i,"jsdjgzbz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jsdjgzbz")

	fn=""
	FOR i=1 TO FCOUNT("jsdjgzbz")
	    fn=fn+","+FIELD(i,"jsdjgzbz")+" jsdjgzbz."+FIELD(i,"jsdjgzbz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jsdjgzbz")

	CURSORSETPROP("sendupdates",.t.,"jsdjgzbz")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT jsdjgzbz
INDEX ON tbnd+zwbm TAG ndbm ADDITIVE
INDEX ON tbnd TAG tbnd ADDITIVE
