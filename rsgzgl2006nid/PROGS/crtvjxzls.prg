FUNCTION crtvjxzls

PARAMETERS ttUpdate,tcConn

IF USED("jxzls")
    USE IN jxzls
ENDIF
SQLEXEC(tcConn,"select * from jxzls","jxzls")

IF ttUpdate
	CURSORSETPROP("tables","jxzls","jxzls")
	CURSORSETPROP("keyfieldlist","nd,dwbm","jxzls")

	fn=""
	FOR i=1 TO FCOUNT("jxzls")
	    fn=fn+","+FIELD(i,"jxzls")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jxzls")

	fn=""
	FOR i=1 TO FCOUNT("jxzls")
	    fn=fn+","+FIELD(i,"jxzls")+" jxzls."+FIELD(i,"jxzls")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jxzls")

	CURSORSETPROP("sendupdates",.t.,"jxzls")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT jxzls
INDEX ON dwbm+nd TAG bmnd ADDITIVE
