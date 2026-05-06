FUNCTION crtvjdzw

PARAMETERS ttUpdates,tcConn

IF USED("jdzw")
    USE IN jdzw
ENDIF

SQLEXEC(tcConn,"select * from jdzw where dwbm='"+m.pdwbm+"'","jdzw")

IF ttUpdates
	CURSORSETPROP("tables","jdzw","jdzw")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,zjbm","jdzw")

	fn=""
	FOR i=1 TO FCOUNT("jdzw")
	    IF UPPER(FIELD(i,"jdzw"))<>'ID'
            fn=fn+","+FIELD(i,"jdzw")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jdzw")

	fn=""
	FOR i=1 TO FCOUNT("jdzw")
        fn=fn+","+FIELD(i,"jdzw")+" jdzw."+FIELD(i,"jdzw")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jdzw")

	CURSORSETPROP("sendupdates",.t.,"jdzw")
	CURSORSETPROP("wheretype",2,"jdzw")
ENDIF

SELECT jdzw
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE