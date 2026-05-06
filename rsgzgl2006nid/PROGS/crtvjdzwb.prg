FUNCTION crtvjdzwb

PARAMETERS ttUpdates,tcConn

IF USED("jdzwb")
    USE IN jdzwb
ENDIF


SQLEXEC(tcConn,"select * from jdzwb where dwbm='"+m.pdwbm+"'","jdzwb")

IF ttUpdates
	CURSORSETPROP("tables","jdzwb","jdzwb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,zjbm","jdzwb")

	fn=""
	FOR i=1 TO FCOUNT("jdzwb")
        fn=fn+","+FIELD(i,"jdzwb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jdzwb")

	fn=""
	FOR i=1 TO FCOUNT("jdzwb")
        fn=fn+","+FIELD(i,"jdzwb")+" jdzwb."+FIELD(i,"jdzwb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jdzwb")

	CURSORSETPROP("sendupdates",.t.,"jdzwb")
	CURSORSETPROP("wheretype",2,"jdzwb")
ENDIF

SELECT jdzwb
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE