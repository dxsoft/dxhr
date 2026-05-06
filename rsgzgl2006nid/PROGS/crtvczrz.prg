FUNCTION crtvczrz

PARAMETERS ttUpdates,tcConn

IF USED("czrz")
    USE IN czrz
ENDIF

SQLEXEC(tcConn,"select uid,gn,wj,sj from czrz","czrz")

IF ttUpdates
	CURSORSETPROP("tables","czrz","czrz")
	CURSORSETPROP("keyfieldlist","uid,sj","czrz")

	fn=""
	FOR i=1 TO FCOUNT("czrz")
        fn=fn+","+FIELD(i,"czrz")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"czrz")

	fn=""
	FOR i=1 TO FCOUNT("czrz")
        fn=fn+","+FIELD(i,"czrz")+" czrz."+FIELD(i,"czrz")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"czrz")

	CURSORSETPROP("sendupdates",.t.,"czrz")
	CURSORSETPROP("wheretype",2)
ENDIF