FUNCTION crtvjcglgzbz

PARAMETERS ttUpdate,tcConn

IF USED("jcglgzbz")
    USE IN jcglgzbz
ENDIF

SQLEXEC(tcConn,"select * from jcglgzbz","jcglgzbz")

IF ttUpdate
	CURSORSETPROP("tables","jcglgzbz","jcglgzbz")
	CURSORSETPROP("keyfieldlist","tbnd","jcglgzbz")

	fn=""
	FOR i=1 TO FCOUNT("jcglgzbz")
	    fn=fn+","+FIELD(i,"jcglgzbz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jcglgzbz")

	fn=""
	FOR i=1 TO FCOUNT("jcglgzbz")
	    fn=fn+","+FIELD(i,"jcglgzbz")+" jcglgzbz."+FIELD(i,"jcglgzbz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jcglgzbz")

	CURSORSETPROP("sendupdates",.t.,"jcglgzbz")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT jcglgzbz
INDEX ON tbnd TAG tbnd ADDITIVE
