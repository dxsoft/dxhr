FUNCTION crtvjxgzbz

PARAMETERS ttUpdate,tcConn

IF USED("jxgzbz")
    USE IN jxgzbz
ENDIF
SQLEXEC(tcConn,"select * from jxgzbz","jxgzbz")

IF ttUpdate
	CURSORSETPROP("tables","jxgzbz","jxgzbz")
	CURSORSETPROP("keyfieldlist","tbnd,ryflbm,xl","jxgzbz")

	fn=""
	FOR i=1 TO FCOUNT("jxgzbz")
	    fn=fn+","+FIELD(i,"jxgzbz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jxgzbz")

	fn=""
	FOR i=1 TO FCOUNT("jxgzbz")
	    fn=fn+","+FIELD(i,"jxgzbz")+" jxgzbz."+FIELD(i,"jxgzbz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jxgzbz")

	CURSORSETPROP("sendupdates",.t.,"jxgzbz")
	CURSORSETPROP("wheretype",2)
ENDIF
