FUNCTION crtvjytgzzbf

PARAMETERS ttUpdates,tcConn

IF USED("jytgzzbf")
    USE IN jytgzzbf
ENDIF

SQLEXEC(tcConn,"select * from jytgzzbf where dwbm='"+m.pdwbm+"' order by grbm","jytgzzbf")

IF ttUpdates
	CURSORSETPROP("tables","jytgzzbf","jytgzzbf")
	CURSORSETPROP("keyfieldlist","dwbm,grbm","jytgzzbf")

	fn=""
	FOR i=1 TO FCOUNT("jytgzzbf")
        fn=fn+","+FIELD(i,"jytgzzbf")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jytgzzbf")

	fn=""
	FOR i=1 TO FCOUNT("jytgzzbf")
        fn=fn+","+FIELD(i,"jytgzzbf")+" jytgzzbf."+FIELD(i,"jytgzzbf")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jytgzzbf")

	CURSORSETPROP("sendupdates",.t.,"jytgzzbf")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT jytgzzbf
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE
INDEX ON dwbm TAG dwbm ADDITIVE

