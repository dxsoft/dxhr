FUNCTION crtvjfjs

PARAMETERS ttUpdates,tcConn

IF USED("jfjs")
    USE IN jfjs
ENDIF

SQLEXEC(tcConn,"select * from jfjs where dwbm='"+m.pdwbm+"'","jfjs")

IF ttUpdates
	CURSORSETPROP("tables","jfjs","jfjs")
	CURSORSETPROP("keyfieldlist","id","jfjs")

	fn=""
	FOR i=1 TO FCOUNT("jfjs")
        fn=fn+","+FIELD(i,"jfjs")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jfjs")

	fn=""
	FOR i=1 TO FCOUNT("jfjs")
        fn=fn+","+FIELD(i,"jfjs")+" jfjs."+FIELD(i,"jfjs")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jfjs")

	CURSORSETPROP("sendupdates",.t.,"jfjs")
	CURSORSETPROP("wheretype",2,"jfjs")
ENDIF

SELECT jfjs
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE
INDEX ON dwbm+grbm+nd TAG bmnd ADDITIVE
