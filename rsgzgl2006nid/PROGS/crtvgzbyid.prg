FUNCTION crtvgzbyid

PARAMETERS ttUpdates,tcConn,tId

IF USED("gz")
    USE IN gz
ENDIF

SQLEXEC(tcConn,"select * from hisbase where sid='"+tId+"'","gz")

IF ttUpdates
	CURSORSETPROP("tables","hisbase","gz")
	CURSORSETPROP("keyfieldlist","id","gz")

	fn=""
	FOR i=1 TO FCOUNT("gz")
    	fn=fn+","+FIELD(i,"gz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"gz")

	fn=""
	FOR i=1 TO FCOUNT("gz")
   	    fn=fn+","+FIELD(i,"gz")+" hisbase."+FIELD(i,"gz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"gz")

	CURSORSETPROP("sendupdates",.t.,"gz")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT gz

CURSORSETPROP("Buffering",5,"gz")