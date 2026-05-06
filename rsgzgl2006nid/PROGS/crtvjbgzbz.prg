FUNCTION crtvjbgzbz

PARAMETERS ttUpdate,tcConn

IF USED("jbgzbz")
    USE IN jbgzbz
ENDIF
SQLEXEC(tcConn,"select * from jbgzbz","jbgzbz")

IF ttUpdate
	CURSORSETPROP("tables","jbgzbz","jbgzbz")
	CURSORSETPROP("keyfieldlist","tbnd","jbgzbz")

	fn=""
	FOR i=1 TO FCOUNT("jbgzbz")
	    fn=fn+","+FIELD(i,"jbgzbz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jbgzbz")

	fn=""
	FOR i=1 TO FCOUNT("jbgzbz")
	    fn=fn+","+FIELD(i,"jbgzbz")+" jbgzbz."+FIELD(i,"jbgzbz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jbgzbz")

	CURSORSETPROP("sendupdates",.t.,"jbgzbz")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT jbgzbz
INDEX ON tbnd TAG tbnd ADDITIVE
