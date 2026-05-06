FUNCTION crtvtgqgzb

PARAMETERS ttUpdate,tcConn

SQLEXEC(tcConn,"select * from tgqgz2006b where dwbm='"+m.pdwbm+"'","tgqgz2006b")

IF ttUpdate
	CURSORSETPROP("tables","tgqgz2006b","tgqgz2006b")
	CURSORSETPROP("keyfieldlist","dwbm,grbm","tgqgz2006b")

	fn=""
	FOR i=1 TO FCOUNT("tgqgz2006b")
	    fn=fn+","+FIELD(i,"tgqgz2006b")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"tgqgz2006b")

	fn=""
	FOR i=1 TO FCOUNT("tgqgz2006b")
	    fn=fn+","+FIELD(i,"tgqgz2006b")+" tgqgz2006b."+FIELD(i,"tgqgz2006b")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"tgqgz2006b")

	CURSORSETPROP("sendupdates",.t.,"tgqgz2006b")
	CURSORSETPROP("wheretype",2)
ENDIF
SELECT tgqgz2006b
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE