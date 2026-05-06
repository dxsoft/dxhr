FUNCTION crtvjxzl

PARAMETERS ttUpdate,tcConn

IF USED("jxzl")
    USE IN jxzl
ENDIF
SQLEXEC(tcConn,"select * from jxzl","jxzl")

IF ttUpdate
	CURSORSETPROP("tables","jxzl","jxzl")
	CURSORSETPROP("keyfieldlist","nd,dwbm","jxzl")

	fn=""
	FOR i=1 TO FCOUNT("jxzl")
	    fn=fn+","+FIELD(i,"jxzl")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jxzl")

	fn=""
	FOR i=1 TO FCOUNT("jxzl")
	    fn=fn+","+FIELD(i,"jxzl")+" jxzl."+FIELD(i,"jxzl")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jxzl")

	CURSORSETPROP("sendupdates",.t.,"jxzl")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT jxzl
INDEX ON dwbm+nd TAG bmnd ADDITIVE
