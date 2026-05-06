FUNCTION crtvhjxx

PARAMETERS ttUpdates,tcConn

IF USED("hjxx")
    USE IN hjxx
ENDIF

SQLEXEC(tcConn,"select * from hjxx where dwbm='"+ALLTRIM(m.pdwbm)+"'","hjxx")

IF ttUpdates
	CURSORSETPROP("tables","hjxx","hjxx")

*!*		CURSORSETPROP("keyfieldlist","dwbm,grbm,hjmc,hjsj","hjxx")

	CURSORSETPROP("keyfieldlist","id","hjxx")

	fn=""
	FOR i=1 TO FCOUNT("hjxx")
    	IF FIELD(i,"hjxx")<>"ID"
            fn=fn+","+FIELD(i,"hjxx")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"hjxx")

	fn=""
	FOR i=1 TO FCOUNT("hjxx")
        fn=fn+","+FIELD(i,"hjxx")+" hjxx."+FIELD(i,"hjxx")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"hjxx")

	CURSORSETPROP("sendupdates",.t.,"hjxx")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT hjxx
INDEX ON dwbm+grbm+tqyjjssj TAG bm addi
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE

INDEX ON dwbm+grbm+jllx+hjsj TAG bmlb ADDITIVE