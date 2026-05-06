FUNCTION crtvjxjtbz

PARAMETERS ttUpdate,tcConn

IF USED("jxjtbz")
    USE IN jxjtbz
ENDIF
SQLEXEC(tcConn,"select * from jxjtbz","jxjtbz")

IF ttUpdate
	CURSORSETPROP("tables","jxjtbz","jxjtbz")
	CURSORSETPROP("keyfieldlist","tbnd,jxbm,lb","jxjtbz")

	fn=""
	FOR i=1 TO FCOUNT("jxjtbz")
	    fn=fn+","+FIELD(i,"jxjtbz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jxjtbz")

	fn=""
	FOR i=1 TO FCOUNT("jxjtbz")
	    fn=fn+","+FIELD(i,"jxjtbz")+" jxjtbz."+FIELD(i,"jxjtbz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jxjtbz")

	CURSORSETPROP("sendupdates",.t.,"jxjtbz")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT jxjtbz
*!*	INDEX ON lb+tbnd+jx TAG NDJX ADDITIVE

INDEX ON tbnd+jx TAG NDJX ADDITIVE

INDEX ON lb+tbnd TAG TBND ADDITIVE
