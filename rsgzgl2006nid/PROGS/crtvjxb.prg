FUNCTION crtvjxb

PARAMETERS ttUpdates,tcConn

IF USED("jxb")
    USE IN jxb
ENDIF

SQLEXEC(tcConn,"select * from jxb where dwbm='"+m.pdwbm+"'","jxb")

IF ttUpdates
	CURSORSETPROP("tables","jxb","jxb")
	CURSORSETPROP("keyfieldlist","id","jxb")

	fn=""
	FOR i=1 TO FCOUNT("jxb")
	    IF FIELD(i,"jxb")<>"ID"
            fn=fn+","+FIELD(i,"jxb")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jxb")

	fn=""
	FOR i=1 TO FCOUNT("jxb")
        fn=fn+","+FIELD(i,"jxb")+" jxb."+FIELD(i,"jxb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jxb")

	CURSORSETPROP("sendupdates",.t.,"jxb")
	CURSORSETPROP("wheretype",2,"jxb")
ENDIF

SELECT jxb
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE
INDEX ON sysj TAG sysj ADDITIVE
INDEX ON dwbm+grbm+jx TAG bmjx ADDITIVE
