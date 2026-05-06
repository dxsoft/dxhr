FUNCTION crtvnjbt

PARAMETERS ttUpdate,tcConn

IF USED("njbt")
    USE IN njbt
ENDIF
SQLEXEC(tcConn,"select * from njbt","njbt")

IF ttUpdate
	CURSORSETPROP("tables","njbt","njbt")
	CURSORSETPROP("keyfieldlist","tbnd","njbt")

	fn=""
	FOR i=1 TO FCOUNT("njbt")
	    fn=fn+","+FIELD(i,"njbt")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"njbt")

	fn=""
	FOR i=1 TO FCOUNT("njbt")
	    fn=fn+","+FIELD(i,"njbt")+" njbt."+FIELD(i,"njbt")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"njbt")

	CURSORSETPROP("sendupdates",.t.,"njbt")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT njbt
INDEX ON tbnd TAG TBND ADDITIVE