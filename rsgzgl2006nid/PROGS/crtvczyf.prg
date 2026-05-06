FUNCTION crtvczyf

PARAMETERS ttUpdate,tcConn

IF USED("czyf")
    USE IN czyf
ENDIF
SQLEXEC(tcConn,"select * from czyf","czyf")

IF ttUpdate
	CURSORSETPROP("tables","czyf","czyf")
	CURSORSETPROP("keyfieldlist","tbnd,bm","czyf")

	fn=""
	FOR i=1 TO FCOUNT("czyf")
	    fn=fn+","+FIELD(i,"czyf")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"czyf")

	fn=""
	FOR i=1 TO FCOUNT("czyf")
	    fn=fn+","+FIELD(i,"czyf")+" czyf."+FIELD(i,"czyf")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"czyf")

	CURSORSETPROP("sendupdates",.t.,"czyf")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT czyf
INDEX ON tbnd+bm TAG ndbm ADDITIVE
INDEX ON tbnd TAG tbnd ADDITIVE
