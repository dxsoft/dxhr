FUNCTION crtvqtqkb

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select * from qtqkb where dwbm='"+m.pdwbm+"'","qtqkb")

IF ttUpdates
	CURSORSETPROP("tables","qtqkb","qtqkb")
	CURSORSETPROP("keyfieldlist","id","qtqkb")

	fn=""
	FOR i=1 TO FCOUNT("qtqkb")
	    IF FIELD(i,"qtqkb")<>"ID"
            fn=fn+","+FIELD(i,"qtqkb")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"qtqkb")

	fn=""
	FOR i=1 TO FCOUNT("qtqkb")
        fn=fn+","+FIELD(i,"qtqkb")+" qtqkb."+FIELD(i,"qtqkb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"qtqkb")

	CURSORSETPROP("sendupdates",.t.,"qtqkb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT qtqkb
INDEX ON dwbm+grbm+jslb+jsnf+jsyf TAG bmlb ADDITIVE