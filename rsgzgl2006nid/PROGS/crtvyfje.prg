FUNCTION crtvyfje

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select * from yfje where dwbm='"+m.pdwbm+"'","yfje")

IF ttUpdates
	CURSORSETPROP("tables","yfje","yfje")
	CURSORSETPROP("keyfieldlist","dwbm,grbm","yfje")

	fn=""
	FOR i=1 TO FCOUNT("yfje")
        fn=fn+","+FIELD(i,"yfje")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"yfje")

	fn=""
	FOR i=1 TO FCOUNT("yfje")
        fn=fn+","+FIELD(i,"yfje")+" yfje."+FIELD(i,"yfje")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"yfje")

	CURSORSETPROP("sendupdates",.t.,"yfje")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT yfje
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE

