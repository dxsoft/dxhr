FUNCTION crtvpajsj

PARAMETERS ttUpdate,tcConn

IF USED("pajsj")
    USE IN pajsj
ENDIF
SQLEXEC(tcConn,"select * from pajsj","pajsj")

IF ttUpdate
	CURSORSETPROP("tables","pajsj","pajsj")
	CURSORSETPROP("keyfieldlist","id","pajsj")

	fn=""
	FOR i=1 TO FCOUNT("pajsj")
	    fn=fn+","+FIELD(i,"pajsj")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"pajsj")

	fn=""
	FOR i=1 TO FCOUNT("pajsj")
	    fn=fn+","+FIELD(i,"pajsj")+" pajsj."+FIELD(i,"pajsj")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"pajsj")

	CURSORSETPROP("sendupdates",.t.,"pajsj")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT pajsj

