FUNCTION crtvqtqk

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select * from qtqk where dwbm='"+m.pdwbm+"'","qtqk")

IF ttUpdates
	CURSORSETPROP("tables","qtqk","qtqk")
	CURSORSETPROP("keyfieldlist","id","qtqk")

	fn=""
	FOR i=1 TO FCOUNT("qtqk")
	    IF FIELD(i,"qtqk")<>"ID"
            fn=fn+","+FIELD(i,"qtqk")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"qtqk")

	fn=""
	FOR i=1 TO FCOUNT("qtqk")
        fn=fn+","+FIELD(i,"qtqk")+" qtqk."+FIELD(i,"qtqk")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"qtqk")

	CURSORSETPROP("sendupdates",.t.,"qtqk")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT qtqk
INDEX ON dwbm+grbm+jslb+jsnf+jsyf TAG bmlb ADDITIVE