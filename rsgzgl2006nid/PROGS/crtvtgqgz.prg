FUNCTION crtvtgqgz

PARAMETERS ttUpdate,tcConn

SQLEXEC(tcConn,"select * from tgqgz2006 where dwbm='"+m.pdwbm+"'","tgqgz2006")

IF ttUpdate
	CURSORSETPROP("tables","tgqgz2006","tgqgz2006")
	CURSORSETPROP("keyfieldlist","dwbm,grbm","tgqgz2006")

	fn=""
	FOR i=1 TO FCOUNT("tgqgz2006")
	    fn=fn+","+FIELD(i,"tgqgz2006")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"tgqgz2006")

	fn=""
	FOR i=1 TO FCOUNT("tgqgz2006")
	    fn=fn+","+FIELD(i,"tgqgz2006")+" tgqgz2006."+FIELD(i,"tgqgz2006")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"tgqgz2006")

	CURSORSETPROP("sendupdates",.t.,"tgqgz2006")
	CURSORSETPROP("wheretype",2)
ENDIF
SELECT tgqgz2006
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE