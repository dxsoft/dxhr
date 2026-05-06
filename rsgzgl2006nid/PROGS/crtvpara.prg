FUNCTION crtvpara

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select * from cyxx","cyxx")

IF ttUpdates
	CURSORSETPROP("tables","cyxx","cyxx")
	CURSORSETPROP("keyfieldlist","","cyxx")

	fn=""
	FOR i=1 TO FCOUNT("cyxx")
        fn=fn+","+FIELD(i,"cyxx")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"cyxx")

	fn=""
	FOR i=1 TO FCOUNT("cyxx")
        fn=fn+","+FIELD(i,"cyxx")+" cyxx."+FIELD(i,"cyxx")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"cyxx")

	CURSORSETPROP("sendupdates",.t.,"cyxx")
	CURSORSETPROP("wheretype",2)
ENDIF
