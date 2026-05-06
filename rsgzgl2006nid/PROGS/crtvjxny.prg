FUNCTION crtvjxny

PARAMETERS ttUpdate,tcConn

IF USED("jxny")
    USE IN jxny
ENDIF
SQLEXEC(tcConn,"select * from jxny","jxny")

IF ttUpdate
	CURSORSETPROP("tables","jxny","jxny")
	CURSORSETPROP("keyfieldlist","cstart,cend","jxny")

	fn=""
	FOR i=1 TO FCOUNT("jxny")
	    fn=fn+","+FIELD(i,"jxny")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jxny")

	fn=""
	FOR i=1 TO FCOUNT("jxny")
	    fn=fn+","+FIELD(i,"jxny")+" jxny."+FIELD(i,"jxny")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jxny")

	CURSORSETPROP("sendupdates",.t.,"jxny")
	CURSORSETPROP("wheretype",2)
ENDIF
